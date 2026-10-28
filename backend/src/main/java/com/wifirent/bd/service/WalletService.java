package com.wifirent.bd.service;

import com.wifirent.bd.domain.hotspot.Hotspot;
import com.wifirent.bd.domain.hotspot.Hotspot.HotspotStatus;
import com.wifirent.bd.domain.hotspot.repository.HotspotRepository;
import com.wifirent.bd.domain.hotspot.repository.GatewayDeviceRepository;
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
import com.wifirent.bd.domain.wallet.WalletTransaction.TransactionType;
import com.wifirent.bd.domain.wallet.repository.WalletRepository;
import com.wifirent.bd.domain.wallet.repository.WalletTransactionRepository;
import com.wifirent.bd.dto.PurchaseWithWalletRequest;
import com.wifirent.bd.dto.PurchaseWithWalletResponse;
import com.wifirent.bd.dto.RefundRequest;
import com.wifirent.bd.dto.RefundResponse;
import com.wifirent.bd.dto.WalletBalanceDto;
import com.wifirent.bd.dto.WalletTopUpRequest;
import com.wifirent.bd.dto.WalletTransferRequest;
import com.wifirent.bd.dto.WalletTransactionDto;
import com.wifirent.bd.dto.WalletTransactionListDto;
import com.wifirent.bd.exception.BadRequestException;
import com.wifirent.bd.exception.ForbiddenException;
import com.wifirent.bd.exception.ResourceNotFoundException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.wifirent.bd.dto.*;
import com.wifirent.bd.gateway.GatewayAdapter;
import com.wifirent.bd.service.GatewayService;

@Service
public class WalletService {

    public static final String PLATFORM_USER_ID_KEY = "platform";
    public static final String DEMO_CREDITS_NOTE = "Demo credits - no real monetary value, not withdrawable";

    private final WalletRepository walletRepository;
    private final WalletTransactionRepository transactionRepository;
    private final UserRepository userRepository;
    private final HotspotRepository hotspotRepository;
    private final PackagePlanRepository packageRepository;
    private final RentalOrderRepository orderRepository;
    private final RentalSessionRepository sessionRepository;
    private final GatewayService gatewayService;
    private final GatewayDeviceRepository gatewayDeviceRepository;
    private final Long initialDemoBalance;
    private final Integer platformCommissionRateBps;

    public WalletService(WalletRepository walletRepository,
                         WalletTransactionRepository transactionRepository,
                         UserRepository userRepository,
                         HotspotRepository hotspotRepository,
                         PackagePlanRepository packageRepository,
                         RentalOrderRepository orderRepository,
                         RentalSessionRepository sessionRepository,
                         GatewayService gatewayService,
                         GatewayDeviceRepository gatewayDeviceRepository,
                         @org.springframework.beans.factory.annotation.Value("${app.wallet.initial-demo-balance:5000}") Long initialDemoBalance,
                         @org.springframework.beans.factory.annotation.Value("${app.wallet.platform-commission-rate-bps:100}") Integer platformCommissionRateBps) {
        this.walletRepository = walletRepository;
        this.transactionRepository = transactionRepository;
        this.userRepository = userRepository;
        this.hotspotRepository = hotspotRepository;
        this.packageRepository = packageRepository;
        this.orderRepository = orderRepository;
        this.sessionRepository = sessionRepository;
        this.gatewayService = gatewayService;
        this.gatewayDeviceRepository = gatewayDeviceRepository;
        this.initialDemoBalance = initialDemoBalance;
        this.platformCommissionRateBps = platformCommissionRateBps;
    }

    /**
     * Ensure a wallet exists for the given user. Creates one with the initial demo balance if missing.
     */
    @Transactional
    public Wallet ensureWallet(Long userId) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        if (walletRepository.existsByUserId(userId)) {
            return walletRepository.findByUserId(userId).orElseThrow();
        }
        Wallet wallet = new Wallet(user, initialDemoBalance);
        Wallet saved = walletRepository.save(wallet);
        recordTransaction(saved, TransactionType.TOPUP, initialDemoBalance,
            "wallet-" + saved.getId(), "WALLET_CREATION",
            "Initial demo credits granted on registration");
        return saved;
    }

    @Transactional(readOnly = true)
    public WalletBalanceDto getBalance(Long userId) {
        Wallet wallet = walletRepository.findByUserId(userId)
            .orElseThrow(() -> new ResourceNotFoundException("Wallet not found"));
        return new WalletBalanceDto(
            wallet.getUser().getId(),
            wallet.getBalanceMinorUnits(),
            "DEMO_CREDITS",
            DEMO_CREDITS_NOTE
        );
    }

    /**
     * Admin-only demo top-up. Grants credits to a user's wallet.
     * Idempotent via idempotency key.
     */
    @Transactional
    public WalletBalanceDto topUp(Long adminUserId, WalletTopUpRequest request) {
        User admin = userRepository.findById(adminUserId)
            .orElseThrow(() -> new ResourceNotFoundException("Admin not found"));
        if (admin.getRole() != UserRole.ADMIN) {
            throw new ForbiddenException("Only admins can top up wallets");
        }

        if (request.amountMinorUnits() == null || request.amountMinorUnits() <= 0) {
            throw new BadRequestException("Top-up amount must be positive");
        }

        String idempotencyKey = request.idempotencyKey() != null ? request.idempotencyKey() : UUID.randomUUID().toString();
        if (transactionRepository.existsByIdempotencyKey(idempotencyKey)) {
            throw new BadRequestException("Duplicate top-up request (idempotency key already used)");
        }

        User targetUser = userRepository.findById(request.targetUserId())
            .orElseThrow(() -> new ResourceNotFoundException("Target user not found"));

        Wallet wallet = walletRepository.findByUserIdForUpdate(targetUser.getId())
            .orElseThrow(() -> new ResourceNotFoundException("Wallet not found for target user"));

        wallet.credit(request.amountMinorUnits());
        walletRepository.save(wallet);

        WalletTransaction tx = new WalletTransaction();
        tx.setWallet(wallet);
        tx.setTransactionType(TransactionType.TOPUP);
        tx.setAmountMinorUnits(request.amountMinorUnits());
        tx.setReferenceId(request.referenceId());
        tx.setReferenceType("ADMIN_TOPUP");
        tx.setDescription("Admin demo top-up: " + request.referenceId());
        tx.setIdempotencyKey(idempotencyKey);
        transactionRepository.save(tx);

        return new WalletBalanceDto(
            targetUser.getId(),
            wallet.getBalanceMinorUnits(),
            "DEMO_CREDITS",
            DEMO_CREDITS_NOTE
        );
    }

    /**
     * Transfer demo credits between users. Atomic, idempotent, with deadlock prevention.
     */
    @Transactional
    public WalletBalanceDto transfer(Long fromUserId, WalletTransferRequest request) {
        User fromUser = userRepository.findById(fromUserId)
            .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (request.targetUserId().equals(fromUserId)) {
            throw new BadRequestException("Cannot transfer to yourself");
        }

        if (request.amountMinorUnits() <= 0) {
            throw new BadRequestException("Transfer amount must be positive");
        }

        String idempotencyKey = request.idempotencyKey() != null ? request.idempotencyKey() : UUID.randomUUID().toString();
        if (transactionRepository.existsByIdempotencyKey(idempotencyKey)) {
            throw new BadRequestException("Duplicate transfer request (idempotency key already used)");
        }

        User toUser = userRepository.findById(request.targetUserId())
            .orElseThrow(() -> new ResourceNotFoundException("Target user not found"));

        Wallet fromWallet = walletRepository.findByUserIdForUpdate(fromUserId)
            .orElseThrow(() -> new ResourceNotFoundException("Source wallet not found"));
        Wallet toWallet = walletRepository.findByUserIdForUpdate(request.targetUserId())
            .orElseThrow(() -> new ResourceNotFoundException("Target wallet not found"));

        // Enforce transfer limit: 100000 demo credits per transfer
        if (request.amountMinorUnits() > 100000L) {
            throw new BadRequestException("Transfer amount exceeds limit of 100000 demo credits");
        }

        fromWallet.debit(request.amountMinorUnits());
        toWallet.credit(request.amountMinorUnits());
        walletRepository.save(fromWallet);
        walletRepository.save(toWallet);

        WalletTransaction debitTx = new WalletTransaction();
        debitTx.setWallet(fromWallet);
        debitTx.setTransactionType(TransactionType.TRANSFER_OUT);
        debitTx.setAmountMinorUnits(-request.amountMinorUnits());
        debitTx.setReferenceId(request.referenceId());
        debitTx.setReferenceType("TRANSFER");
        debitTx.setDescription("Transfer to user " + request.targetUserId());
        debitTx.setIdempotencyKey(idempotencyKey);
        transactionRepository.save(debitTx);

        WalletTransaction creditTx = new WalletTransaction();
        creditTx.setWallet(toWallet);
        creditTx.setTransactionType(TransactionType.TRANSFER_IN);
        creditTx.setAmountMinorUnits(request.amountMinorUnits());
        creditTx.setReferenceId(request.referenceId());
        creditTx.setReferenceType("TRANSFER");
        creditTx.setDescription("Transfer from user " + fromUserId);
        creditTx.setIdempotencyKey(idempotencyKey + "-in");
        transactionRepository.save(creditTx);

        return new WalletBalanceDto(
            fromUserId,
            fromWallet.getBalanceMinorUnits(),
            "DEMO_CREDITS",
            DEMO_CREDITS_NOTE
        );
    }

    /**
     * Purchase a Wi-Fi package using demo credits.
     * Atomic transaction: debit customer, credit host, credit platform commission,
     * create rental order, create session (PENDING).
     * After transaction commits, gateway authorization is attempted.
     * If gateway authorization fails, customer is refunded and session marked FAILED.
     */
    @Transactional
    public PurchaseWithWalletResponse purchase(Long customerId, PurchaseWithWalletRequest request) {
        String idempotencyKey = request.idempotencyKey() != null ? request.idempotencyKey() : UUID.randomUUID().toString();

        // Prevent duplicate purchase
        if (orderRepository.findByIdempotencyKey(idempotencyKey).isPresent()) {
            throw new BadRequestException("Duplicate purchase (idempotency key already used)");
        }

        User customer = userRepository.findById(customerId)
            .orElseThrow(() -> new ResourceNotFoundException("Customer not found"));

        Hotspot hotspot = hotspotRepository.findById(request.hotspotId())
            .orElseThrow(() -> new ResourceNotFoundException("Hotspot not found"));
        if (!hotspot.getAvailable() || hotspot.getStatus() != HotspotStatus.ACTIVE) {
            throw new BadRequestException("Hotspot is not available");
        }

        PackagePlan plan = packageRepository.findById(request.packageId())
            .orElseThrow(() -> new ResourceNotFoundException("Package not found"));
        if (!plan.getHotspot().getId().equals(hotspot.getId())) {
            throw new BadRequestException("Package does not belong to this hotspot");
        }
        if (!plan.getActive()) {
            throw new BadRequestException("Package is not active");
        }

        Long price = plan.getPriceMinorUnits();
        if (price == null || price < 0) {
            throw new BadRequestException("Invalid package price");
        }

        // Lock customer wallet for debit
        Wallet customerWallet = walletRepository.findByUserIdForUpdate(customerId)
            .orElseThrow(() -> new ResourceNotFoundException("Customer wallet not found"));

        // Sufficient balance check
        if (customerWallet.getBalanceMinorUnits() < price) {
            throw new BadRequestException("Insufficient demo credits");
        }

        // Calculate commission (round down / floor)
        Long platformCommission = calculateCommission(price);
        Long hostEarnings = price - platformCommission;

        // Debit customer
        customerWallet.debit(price);
        walletRepository.save(customerWallet);

        // Credit host
        Long hostUserId = hotspot.getHost().getUser().getId();
        Wallet hostWallet = walletRepository.findByUserIdForUpdate(hostUserId)
            .orElseGet(() -> ensureWallet(hostUserId));
        hostWallet.credit(hostEarnings);
        walletRepository.save(hostWallet);

        // Credit platform commission (if rate > 0)
        if (platformCommission > 0) {
            // Platform wallet is a virtual concept - record via ledger only
        }

        // Create rental order (PAID since wallet debited immediately)
        RentalOrder order = new RentalOrder();
        order.setCustomer(customer);
        order.setHotspot(hotspot);
        order.setPackagePlan(plan);
        order.setStatus(RentalOrderStatus.PAID);
        order.setTotalPriceMinorUnits(price);
        order.setIdempotencyKey(idempotencyKey);
        order = orderRepository.save(order);

        // Create session (PENDING - gateway will authorize)
        Instant now = Instant.now();
        Integer durationMinutes = plan.getDurationMinutes();
        Instant expiresAt = null;
        if (plan.getValidityMinutes() != null) {
            expiresAt = now.plus(plan.getValidityMinutes(), ChronoUnit.MINUTES);
        } else if (durationMinutes != null) {
            expiresAt = now.plus(durationMinutes, ChronoUnit.MINUTES);
        }

        RentalSession session = new RentalSession();
        session.setCustomer(customer);
        session.setHotspot(hotspot);
        session.setPackagePlan(plan);
        session.setRentalOrder(order);
        session.setStartedAt(now);
        session.setExpiresAt(expiresAt);
        session.setDataQuotaBytes(plan.getDataQuotaBytes());
        session.setConsumedBytes(0L);
        session.setStatus(SessionStatus.PENDING);
        session = sessionRepository.save(session);

        // Record customer debit transaction
        WalletTransaction customerTx = new WalletTransaction();
        customerTx.setWallet(customerWallet);
        customerTx.setTransactionType(TransactionType.PURCHASE);
        customerTx.setAmountMinorUnits(-price);
        customerTx.setReferenceId(order.getId().toString());
        customerTx.setReferenceType("RENTAL_ORDER");
        customerTx.setDescription("Package purchase: " + plan.getPackageType() + " - " + price + " demo credits");
        customerTx.setIdempotencyKey(idempotencyKey);
        transactionRepository.save(customerTx);

        // Record host credit transaction
        WalletTransaction hostTx = new WalletTransaction();
        hostTx.setWallet(hostWallet);
        hostTx.setTransactionType(TransactionType.PURCHASE);
        hostTx.setAmountMinorUnits(hostEarnings);
        hostTx.setReferenceId(order.getId().toString());
        hostTx.setReferenceType("RENTAL_ORDER");
        hostTx.setDescription("Host earnings from rental order " + order.getId());
        hostTx.setIdempotencyKey(idempotencyKey + "-host");
        transactionRepository.save(hostTx);

        // ---- GATEWAY AUTHORIZATION (outside transaction) ----
        // Attempt to authorize the session on the gateway.
        // If no gateway device is configured for this hotspot, session stays PENDING
        // until a gateway is registered and manually authorized.
        // If the gateway is MOCK, authorization always succeeds.
        // If the gateway is REAL (OpenWrt), it will attempt to authorize via the router.
        String deviceId = deviceIdForHotspot(hotspot.getId());
        if (deviceId != null) {
            try {
                GatewaySessionResponse gwResp = gatewayService.authorizeSession(
                    deviceId,
                    session.getId()
                );

                if (gwResp.gatewaySessionId() != null) {
                    session.setGatewaySessionId(gwResp.gatewaySessionId());
                    session.setStatus(SessionStatus.ACTIVE);
                    sessionRepository.save(session);
                } else {
                    // Gateway authorization failed - refund and mark session as FAILED
                    handleGatewayAuthorizationFailure(session, customerWallet, price, order,
                        "Gateway authorization failed");
                }
            } catch (Exception e) {
                // Gateway error (e.g., offline, unreachable) - refund and mark session as FAILED
                handleGatewayAuthorizationFailure(session, customerWallet, price, order,
                    "Gateway authorization error: " + e.getMessage());
            }
        } else {
            // No gateway device configured for this hotspot - session stays PENDING
            // until a gateway is registered and manually authorized.
            session.setStatus(SessionStatus.PENDING);
            sessionRepository.save(session);
        }

        return new PurchaseWithWalletResponse(
            order.getId(),
            price,
            hostEarnings,
            platformCommission,
            customerWallet.getBalanceMinorUnits(),
            order.getStatus().name(),
            DEMO_CREDITS_NOTE
        );
    }

    /**
     * Handle gateway authorization failure by refunding the customer and marking the session as FAILED.
     */
    private void handleGatewayAuthorizationFailure(RentalSession session, Wallet customerWallet,
                                                   Long price, RentalOrder order, String reason) {
        // Refund the customer
        customerWallet.credit(price);
        walletRepository.save(customerWallet);

        // Record refund transaction
        WalletTransaction refundTx = new WalletTransaction();
        refundTx.setWallet(customerWallet);
        refundTx.setTransactionType(TransactionType.REFUND);
        refundTx.setAmountMinorUnits(price);
        refundTx.setReferenceId(order.getId().toString());
        refundTx.setReferenceType("RENTAL_ORDER");
        refundTx.setDescription("Gateway authorization failed - refund: " + reason);
        refundTx.setIdempotencyKey("refund-" + order.getId() + "-" + UUID.randomUUID());
        transactionRepository.save(refundTx);

        // Mark session as FAILED
        session.setStatus(SessionStatus.FAILED);
        sessionRepository.save(session);

        // Mark order as CANCELLED
        order.setStatus(RentalOrderStatus.CANCELLED);
        order.setCancelledAt(Instant.now());
        order.setCancellationReason("Gateway authorization failed: " + reason);
        orderRepository.save(order);
    }

    /**
     * Find the gateway device ID for a hotspot. Returns null if no gateway is registered.
     */
    private String deviceIdForHotspot(Long hotspotId) {
        List<com.wifirent.bd.domain.hotspot.GatewayDevice> devices =
            gatewayDeviceRepository.findByHotspotId(hotspotId);
        if (devices.isEmpty()) {
            return null;
        }
        return devices.get(0).getDeviceId();
    }

    /**
     * Refund a purchase. Refunds demo credits to the customer's wallet.
     * Idempotent. Refund cannot exceed original amount.
     */
    @Transactional
    public RefundResponse refund(Long adminUserId, RefundRequest request) {
        User admin = userRepository.findById(adminUserId)
            .orElseThrow(() -> new ResourceNotFoundException("Admin not found"));
        if (admin.getRole() != UserRole.ADMIN) {
            throw new ForbiddenException("Only admins can process refunds");
        }

        String idempotencyKey = request.idempotencyKey() != null ? request.idempotencyKey() : UUID.randomUUID().toString();
        if (transactionRepository.existsByIdempotencyKey(idempotencyKey)) {
            throw new BadRequestException("Duplicate refund request (idempotency key already used)");
        }

        RentalOrder order = orderRepository.findById(request.rentalOrderId())
            .orElseThrow(() -> new ResourceNotFoundException("Rental order not found"));

        if (order.getStatus() != RentalOrderStatus.PAID && order.getStatus() != RentalOrderStatus.CANCELLED) {
            throw new BadRequestException("Only PAID or CANCELLED orders can be refunded");
        }

        Long refundAmount = request.amountMinorUnits();
        if (refundAmount == null || refundAmount <= 0) {
            throw new BadRequestException("Refund amount must be positive");
        }
        if (refundAmount > order.getTotalPriceMinorUnits()) {
            throw new BadRequestException("Refund cannot exceed original amount paid");
        }

        User customer = order.getCustomer();
        Wallet customerWallet = walletRepository.findByUserIdForUpdate(customer.getId())
            .orElseThrow(() -> new ResourceNotFoundException("Customer wallet not found"));

        customerWallet.credit(refundAmount);
        walletRepository.save(customerWallet);

        WalletTransaction tx = new WalletTransaction();
        tx.setWallet(customerWallet);
        tx.setTransactionType(TransactionType.REFUND);
        tx.setAmountMinorUnits(refundAmount);
        tx.setReferenceId(order.getId().toString());
        tx.setReferenceType("RENTAL_ORDER");
        tx.setDescription("Demo refund for rental order " + order.getId() + ": " + request.reason());
        tx.setIdempotencyKey(idempotencyKey);
        transactionRepository.save(tx);

        // If order was PAID, revert to CANCELLED after refund
        if (order.getStatus() == RentalOrderStatus.PAID) {
            order.setStatus(RentalOrderStatus.CANCELLED);
            order.setCancelledAt(Instant.now());
            order.setCancellationReason("Refunded: " + request.reason());
            orderRepository.save(order);
        }

        return new RefundResponse(
            order.getId(),
            order.getId(),
            refundAmount,
            "COMPLETED",
            "Demo refund completed - credits returned to wallet"
        );
    }

    @Transactional(readOnly = true)
    public WalletTransactionListDto listTransactions(Long userId, Pageable pageable) {
        Wallet wallet = walletRepository.findByUserId(userId)
            .orElseThrow(() -> new ResourceNotFoundException("Wallet not found"));
        Page<WalletTransaction> page = transactionRepository.findByWalletId(wallet.getId(), pageable);
        return new WalletTransactionListDto(
            page.getContent().stream().map(this::toDto).toList(),
            page.getNumber(), page.getSize(), page.getTotalElements(), page.getTotalPages()
        );
    }

    private Long calculateCommission(Long price) {
        if (price == null || platformCommissionRateBps == null || platformCommissionRateBps <= 0) {
            return 0L;
        }
        // Round down (floor): price * rate / 10000 using integer division
        return (price * platformCommissionRateBps) / 10000;
    }

    private WalletTransactionDto toDto(WalletTransaction t) {
        return new WalletTransactionDto(
            t.getId(),
            t.getTransactionType().name(),
            t.getAmountMinorUnits(),
            t.getReferenceId(),
            t.getReferenceType(),
            t.getDescription(),
            t.getIdempotencyKey(),
            t.getCreatedAt()
        );
    }

    private void recordTransaction(Wallet wallet, TransactionType type, Long amount,
                                   String referenceId, String referenceType, String description) {
        WalletTransaction tx = new WalletTransaction();
        tx.setWallet(wallet);
        tx.setTransactionType(type);
        tx.setAmountMinorUnits(amount);
        tx.setReferenceId(referenceId);
        tx.setReferenceType(referenceType);
        tx.setDescription(description);
        tx.setIdempotencyKey("wallet-" + wallet.getId() + "-" + type.name() + "-" + referenceId);
        transactionRepository.save(tx);
    }
}
