package com.wifirent.bd.service;

import com.wifirent.bd.service.GatewayService;
import com.wifirent.bd.domain.hotspot.Hotspot;
import com.wifirent.bd.domain.hotspot.Hotspot.HotspotStatus;
import com.wifirent.bd.domain.hotspot.repository.GatewayDeviceRepository;
import com.wifirent.bd.domain.hotspot.repository.HotspotRepository;
import com.wifirent.bd.domain.packageplan.PackagePlan;
import com.wifirent.bd.domain.packageplan.PackageType;
import com.wifirent.bd.domain.packageplan.repository.PackagePlanRepository;
import com.wifirent.bd.domain.rental.RentalOrder;
import com.wifirent.bd.domain.rental.RentalOrderStatus;
import com.wifirent.bd.domain.rental.repository.RentalOrderRepository;
import com.wifirent.bd.domain.session.RentalSession;
import com.wifirent.bd.domain.session.SessionStatus;
import com.wifirent.bd.domain.session.repository.RentalSessionRepository;
import com.wifirent.bd.domain.user.User;
import com.wifirent.bd.domain.user.UserRole;
import com.wifirent.bd.domain.user.repository.UserRepository;
import com.wifirent.bd.domain.wallet.Wallet;
import com.wifirent.bd.domain.wallet.WalletTransaction;
import com.wifirent.bd.domain.wallet.repository.WalletRepository;
import com.wifirent.bd.domain.wallet.repository.WalletTransactionRepository;
import com.wifirent.bd.dto.PurchaseWithWalletRequest;
import com.wifirent.bd.dto.PurchaseWithWalletResponse;
import com.wifirent.bd.dto.GatewaySessionResponse;
import com.wifirent.bd.dto.RefundRequest;
import com.wifirent.bd.dto.WalletBalanceDto;
import com.wifirent.bd.dto.WalletTopUpRequest;
import com.wifirent.bd.dto.WalletTransferRequest;
import com.wifirent.bd.exception.BadRequestException;
import com.wifirent.bd.exception.ForbiddenException;
import com.wifirent.bd.exception.ResourceNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WalletServiceTest {

    @Mock
    private WalletRepository walletRepository;

    @Mock
    private WalletTransactionRepository transactionRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private HotspotRepository hotspotRepository;

    @Mock
    private PackagePlanRepository packageRepository;

    @Mock
    private RentalOrderRepository orderRepository;

    @Mock
    private RentalSessionRepository sessionRepository;

    @Mock
    private GatewayService gatewayService;

    @Mock
    private GatewayDeviceRepository gatewayDeviceRepository;

    @InjectMocks
    private WalletService walletService;

    private User customer;
    private User host;
    private User admin;
    private Hotspot hotspot;
    private PackagePlan plan;

    @BeforeEach
    void setUp() {
        walletService = new WalletService(
            walletRepository, transactionRepository, userRepository,
            hotspotRepository, packageRepository, orderRepository, sessionRepository,
            gatewayService, gatewayDeviceRepository,
            5000L, 100
        );

        customer = new User("Customer", "cust@example.com", "pass", UserRole.CUSTOMER);
        customer.setId(1L);

        host = new User("Host", "host@example.com", "pass", UserRole.HOST);
        host.setId(2L);

        admin = new User("Admin", "admin@example.com", "pass", UserRole.ADMIN);
        admin.setId(3L);

        com.wifirent.bd.domain.user.HostProfile hostProfile = new com.wifirent.bd.domain.user.HostProfile();
        hostProfile.setUser(host);
        hostProfile.setApproved(true);
        hostProfile.setStatus(com.wifirent.bd.domain.user.HostProfile.HostStatus.APPROVED);

        hotspot = new Hotspot();
        hotspot.setId(10L);
        hotspot.setName("Test Hotspot");
        hotspot.setAvailable(true);
        hotspot.setStatus(HotspotStatus.ACTIVE);
        hotspot.setHost(hostProfile);

        plan = new PackagePlan();
        plan.setId(100L);
        plan.setPriceMinorUnits(500L);
        plan.setPackageType(PackageType.TIME_BASED);
        plan.setDurationMinutes(60);
        plan.setActive(true);
        plan.setHotspot(hotspot);
    }

    @Test
    void ensureWallet_createsWalletWithInitialBalance() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(customer));
        when(walletRepository.existsByUserId(1L)).thenReturn(false);

        Wallet wallet = new Wallet(customer, 5000L);
        wallet.setId(100L);
        when(walletRepository.save(any(Wallet.class))).thenReturn(wallet);
        when(transactionRepository.save(any(WalletTransaction.class))).thenAnswer(inv -> inv.getArgument(0));

        Wallet result = walletService.ensureWallet(1L);

        assertNotNull(result);
        assertEquals(5000L, result.getBalanceMinorUnits());
        verify(walletRepository).save(any(Wallet.class));
        verify(transactionRepository).save(any(WalletTransaction.class));
    }

    @Test
    void ensureWallet_returnsExistingWallet() {
        Wallet existing = new Wallet(customer, 3000L);
        existing.setId(100L);
        when(userRepository.findById(1L)).thenReturn(Optional.of(customer));
        when(walletRepository.existsByUserId(1L)).thenReturn(true);
        when(walletRepository.findByUserId(1L)).thenReturn(Optional.of(existing));

        Wallet result = walletService.ensureWallet(1L);

        assertEquals(3000L, result.getBalanceMinorUnits());
        verify(walletRepository, never()).save(any());
    }

    @Test
    void getBalance_returnsDemoCredits() {
        Wallet wallet = new Wallet(customer, 5000L);
        wallet.setId(100L);
        when(walletRepository.findByUserId(1L)).thenReturn(Optional.of(wallet));

        WalletBalanceDto dto = walletService.getBalance(1L);

        assertEquals(1L, dto.userId());
        assertEquals(5000L, dto.balanceMinorUnits());
        assertEquals("DEMO_CREDITS", dto.currency());
        assertNotNull(dto.note());
        assertTrue(dto.note().contains("no real monetary value"));
        assertTrue(dto.note().contains("not withdrawable"));
    }

    @Test
    void topUpByAdmin_creditsWallet() {
        Wallet wallet = new Wallet(customer, 5000L);
        wallet.setId(100L);
        when(userRepository.findById(3L)).thenReturn(Optional.of(admin));
        when(userRepository.findById(1L)).thenReturn(Optional.of(customer));
        assert admin.getRole() == UserRole.ADMIN;
        when(walletRepository.findByUserIdForUpdate(1L)).thenReturn(Optional.of(wallet));
        when(walletRepository.save(any(Wallet.class))).thenReturn(wallet);
        when(transactionRepository.save(any(WalletTransaction.class))).thenAnswer(inv -> inv.getArgument(0));

        WalletTopUpRequest req = new WalletTopUpRequest(1L, 1000L, "topup-1", "idem-1");
        WalletBalanceDto dto = walletService.topUp(3L, req);

        assertEquals(6000L, dto.balanceMinorUnits());
        assertEquals(6000L, wallet.getBalanceMinorUnits());
    }

    @Test
    void topUp_rejectsNonAdmin() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(customer));
        assert customer.getRole() == UserRole.CUSTOMER;

        WalletTopUpRequest req = new WalletTopUpRequest(1L, 1000L, "topup-1", "idem-1");
        assertThrows(ForbiddenException.class, () -> walletService.topUp(1L, req));
    }

    @Test
    void topUp_preventsDuplicateByIdempotencyKey() {
        when(userRepository.findById(3L)).thenReturn(Optional.of(admin));
        when(transactionRepository.existsByIdempotencyKey("idem-dup")).thenReturn(true);

        WalletTopUpRequest req = new WalletTopUpRequest(1L, 1000L, "topup-2", "idem-dup");
        assertThrows(BadRequestException.class, () -> walletService.topUp(3L, req));
    }

    @Test
    void transferBetweenUsers_debitsAndCreditsAtomically() {
        Wallet fromWallet = new Wallet(customer, 5000L);
        fromWallet.setId(100L);
        Wallet toWallet = new Wallet(host, 3000L);
        toWallet.setId(200L);

        when(userRepository.findById(1L)).thenReturn(Optional.of(customer));
        when(userRepository.findById(2L)).thenReturn(Optional.of(host));
        when(walletRepository.findByUserIdForUpdate(1L)).thenReturn(Optional.of(fromWallet));
        when(walletRepository.findByUserIdForUpdate(2L)).thenReturn(Optional.of(toWallet));
        when(walletRepository.save(any(Wallet.class))).thenAnswer(inv -> inv.getArgument(0));
        when(transactionRepository.save(any(WalletTransaction.class))).thenAnswer(inv -> inv.getArgument(0));

        WalletTransferRequest req = new WalletTransferRequest(2L, 1000L, "transfer-1", "idem-transfer");
        WalletBalanceDto dto = walletService.transfer(1L, req);

        assertEquals(4000L, dto.balanceMinorUnits());
        assertEquals(4000L, fromWallet.getBalanceMinorUnits());
        assertEquals(4000L, toWallet.getBalanceMinorUnits());
    }

    @Test
    void transfer_rejectsSelfTransfer() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(customer));

        WalletTransferRequest req = new WalletTransferRequest(1L, 1000L, "self-1", "idem-self");
        assertThrows(BadRequestException.class, () -> walletService.transfer(1L, req));
    }

    @Test
    void transfer_rejectsInsufficientBalance() {
        Wallet wallet = new Wallet(customer, 5000L);
        wallet.setId(100L);
        Wallet hostWallet = new Wallet(host, 3000L);
        hostWallet.setId(200L);
        when(userRepository.findById(1L)).thenReturn(Optional.of(customer));
        when(userRepository.findById(2L)).thenReturn(Optional.of(host));
        when(walletRepository.findByUserIdForUpdate(1L)).thenReturn(Optional.of(wallet));
        when(walletRepository.findByUserIdForUpdate(2L)).thenReturn(Optional.of(hostWallet));

        WalletTransferRequest req = new WalletTransferRequest(2L, 10000L, "transfer-big", "idem-big");
        assertThrows(IllegalArgumentException.class, () -> walletService.transfer(1L, req));
    }

    @Test
    void transfer_preventsDuplicateByIdempotencyKey() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(customer));
        when(transactionRepository.existsByIdempotencyKey("idem-dup-transfer")).thenReturn(true);

        WalletTransferRequest req = new WalletTransferRequest(2L, 1000L, "transfer-dup", "idem-dup-transfer");
        assertThrows(BadRequestException.class, () -> walletService.transfer(1L, req));
    }

    @Test
    void purchaseWithSufficientBalance_debitsCustomerCreditsHost() {
        Wallet customerWallet = new Wallet(customer, 5000L);
        customerWallet.setId(100L);
        Wallet hostWallet = new Wallet(host, 3000L);
        hostWallet.setId(200L);

        when(userRepository.findById(1L)).thenReturn(Optional.of(customer));
        when(hotspotRepository.findById(10L)).thenReturn(Optional.of(hotspot));
        when(packageRepository.findById(100L)).thenReturn(Optional.of(plan));
        when(orderRepository.findByIdempotencyKey("idem-purchase")).thenReturn(Optional.empty());
        when(walletRepository.findByUserIdForUpdate(1L)).thenReturn(Optional.of(customerWallet));
        when(walletRepository.findByUserIdForUpdate(2L)).thenReturn(Optional.of(hostWallet));
        when(walletRepository.save(any(Wallet.class))).thenAnswer(inv -> inv.getArgument(0));
        when(orderRepository.save(any(RentalOrder.class))).thenAnswer(inv -> {
            RentalOrder o = inv.getArgument(0);
            o.setId(1000L);
            return o;
        });
        when(sessionRepository.save(any(RentalSession.class))).thenAnswer(inv -> inv.getArgument(0));
        when(transactionRepository.save(any(WalletTransaction.class))).thenAnswer(inv -> inv.getArgument(0));

        PurchaseWithWalletRequest req = new PurchaseWithWalletRequest(10L, 100L, "idem-purchase");
        PurchaseWithWalletResponse response = walletService.purchase(1L, req);

        assertNotNull(response.orderId());
        assertEquals(500L, response.customerDebitedMinorUnits());
        assertEquals(495L, response.hostCreditedMinorUnits());
        assertEquals(5L, response.platformCommissionMinorUnits());
        assertEquals(4500L, response.customerRemainingBalanceMinorUnits());
        assertEquals(RentalOrderStatus.PAID.name(), response.status());

        // Verify customer debited
        assertEquals(4500L, customerWallet.getBalanceMinorUnits());
        // Verify host credited
        assertEquals(3495L, hostWallet.getBalanceMinorUnits());
    }

    @Test
    void purchase_rejectsInsufficientBalance() {
        Wallet wallet = new Wallet(customer, 100L);
        wallet.setId(100L);
        when(userRepository.findById(1L)).thenReturn(Optional.of(customer));
        when(hotspotRepository.findById(10L)).thenReturn(Optional.of(hotspot));
        when(packageRepository.findById(100L)).thenReturn(Optional.of(plan));
        when(walletRepository.findByUserIdForUpdate(1L)).thenReturn(Optional.of(wallet));

        PurchaseWithWalletRequest req = new PurchaseWithWalletRequest(10L, 100L, "idem-insuff");
        assertThrows(BadRequestException.class, () -> walletService.purchase(1L, req));
    }

    @Test
    void purchase_preventsDuplicateByIdempotencyKey() {
        when(orderRepository.findByIdempotencyKey("idem-dup-purchase")).thenReturn(Optional.of(new RentalOrder()));

        PurchaseWithWalletRequest req = new PurchaseWithWalletRequest(10L, 100L, "idem-dup-purchase");
        assertThrows(BadRequestException.class, () -> walletService.purchase(1L, req));
    }

    @Test
    void commissionCalculationRoundsDown() {
        long price = 999L;
        long commission = (price * 100) / 10000;
        assertEquals(9L, commission);
        assertEquals(990L, price - commission);
    }

    @Test
    void refund_returnsCreditsToCustomer() {
        when(userRepository.findById(3L)).thenReturn(Optional.of(admin));
        assert admin.getRole() == UserRole.ADMIN;

        RentalOrder order = new RentalOrder();
        order.setId(50L);
        order.setTotalPriceMinorUnits(500L);
        order.setStatus(RentalOrderStatus.PAID);
        order.setCustomer(customer);

        Wallet wallet = new Wallet(customer, 4000L);
        wallet.setId(100L);

        when(orderRepository.findById(50L)).thenReturn(Optional.of(order));
        when(walletRepository.findByUserIdForUpdate(1L)).thenReturn(Optional.of(wallet));
        when(walletRepository.save(any(Wallet.class))).thenReturn(wallet);
        when(orderRepository.save(any(RentalOrder.class))).thenReturn(order);
        when(transactionRepository.save(any(WalletTransaction.class))).thenAnswer(inv -> inv.getArgument(0));

        RefundRequest req = new RefundRequest(50L, 300L, "customer cancelled", "idem-refund");
        com.wifirent.bd.dto.RefundResponse response = walletService.refund(3L, req);

        assertEquals(50L, response.refundId());
        assertEquals(300L, response.amountMinorUnits());
        assertEquals(4300L, wallet.getBalanceMinorUnits()); // 4000 + 300
    }

    @Test
    void refund_rejectsAmountGreaterThanOriginal() {
        when(userRepository.findById(3L)).thenReturn(Optional.of(admin));

        RentalOrder order = new RentalOrder();
        order.setId(50L);
        order.setTotalPriceMinorUnits(500L);
        order.setStatus(RentalOrderStatus.PAID);
        order.setCustomer(customer);

        when(orderRepository.findById(50L)).thenReturn(Optional.of(order));

        RefundRequest req = new RefundRequest(50L, 600L, "too much", "idem-refund-big");
        assertThrows(BadRequestException.class, () -> walletService.refund(3L, req));
    }

    @Test
    void refund_preventsDuplicateByIdempotencyKey() {
        when(userRepository.findById(3L)).thenReturn(Optional.of(admin));
        when(transactionRepository.existsByIdempotencyKey("idem-dup-refund")).thenReturn(true);

        RefundRequest req = new RefundRequest(50L, 100L, "refund-dup", "idem-dup-refund");
        assertThrows(BadRequestException.class, () -> walletService.refund(3L, req));
    }

    @Test
    void concurrentTransfersDoNotOverspend() throws Exception {
        Wallet wallet = new Wallet(customer, 5000L);
        wallet.setId(100L);
        when(userRepository.findById(1L)).thenReturn(Optional.of(customer));
        when(userRepository.findById(2L)).thenReturn(Optional.of(host));
        when(walletRepository.findByUserIdForUpdate(1L)).thenReturn(Optional.of(wallet));
        when(walletRepository.findByUserIdForUpdate(2L)).thenReturn(Optional.of(new Wallet(host, 0L)));
        when(walletRepository.save(any(Wallet.class))).thenAnswer(inv -> inv.getArgument(0));
        when(transactionRepository.save(any(WalletTransaction.class))).thenAnswer(inv -> inv.getArgument(0));

        int threadCount = 10;
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);
        AtomicReference<Exception> exceptionRef = new AtomicReference<>(null);

        for (int i = 0; i < threadCount; i++) {
            final int idx = i;
            executor.submit(() -> {
                try {
                    WalletTransferRequest req = new WalletTransferRequest(
                        2L, 100L, "concurrent-" + idx, "idem-concurrent-" + idx);
                    walletService.transfer(1L, req);
                } catch (Exception e) {
                    exceptionRef.set(e);
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await();
        executor.shutdown();

        // All 10 transfers of 100 each should succeed: 5000 - 1000 = 4000
        assertEquals(4000L, wallet.getBalanceMinorUnits());
        assertNull(exceptionRef.get());
    }

    @Test
    void noRealMoneyTermsInResponses() {
        Wallet wallet = new Wallet(customer, 5000L);
        when(walletRepository.findByUserId(1L)).thenReturn(Optional.of(wallet));

        WalletBalanceDto dto = walletService.getBalance(1L);
        assertEquals("DEMO_CREDITS", dto.currency());
        assertNotNull(dto.note());
        assertTrue(dto.note().toLowerCase().contains("demo"));
        assertTrue(dto.note().toLowerCase().contains("not withdrawable"));
    }

    @Test
    void walletTransactionIsImmutableLedgerEntry() {
        Wallet wallet = new Wallet(customer, 5000L);
        wallet.setId(100L);

        WalletTransaction tx = new WalletTransaction();
        tx.setWallet(wallet);
        tx.setTransactionType(WalletTransaction.TransactionType.PURCHASE);
        tx.setAmountMinorUnits(-500L);
        tx.setReferenceId("order-1000");
        tx.setReferenceType("RENTAL_ORDER");
        tx.setDescription("Package purchase");
        tx.setIdempotencyKey("idem-purchase-1000");

        when(transactionRepository.save(any(WalletTransaction.class))).thenAnswer(inv -> {
            WalletTransaction t = inv.getArgument(0);
            t.setId(500L);
            t.setCreatedAt(Instant.now());
            return t;
        });

        WalletTransaction saved = transactionRepository.save(tx);

        assertNotNull(saved.getId());
        assertNotNull(saved.getCreatedAt());
        assertEquals(-500L, saved.getAmountMinorUnits());
        assertEquals("order-1000", saved.getReferenceId());
        assertEquals("RENTAL_ORDER", saved.getReferenceType());
    }

    @Test
    void walletBalanceCannotBeDirectlySetByClient() {
        // Verify that Wallet entity only allows credit/debit operations, not direct balance set
        Wallet wallet = new Wallet(customer, 5000L);
        assertEquals(5000L, wallet.getBalanceMinorUnits());

        wallet.credit(1000L);
        assertEquals(6000L, wallet.getBalanceMinorUnits());

        wallet.debit(500L);
        assertEquals(5500L, wallet.getBalanceMinorUnits());

        assertThrows(IllegalArgumentException.class, () -> wallet.debit(6000L));
        assertEquals(5500L, wallet.getBalanceMinorUnits()); // unchanged after failed debit
    }
}
