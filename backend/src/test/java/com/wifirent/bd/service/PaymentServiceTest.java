package com.wifirent.bd.service;

import com.wifirent.bd.domain.hotspot.Hotspot;
import com.wifirent.bd.domain.user.HostProfile;
import com.wifirent.bd.domain.payment.Payment;
import com.wifirent.bd.domain.payment.PaymentStatus;
import com.wifirent.bd.domain.payment.PaymentProvider;
import com.wifirent.bd.domain.payment.repository.PaymentRepository;
import com.wifirent.bd.domain.rental.RentalOrder;
import com.wifirent.bd.domain.rental.RentalOrderStatus;
import com.wifirent.bd.domain.rental.repository.RentalOrderRepository;
import com.wifirent.bd.domain.session.RentalSession;
import com.wifirent.bd.domain.session.SessionStatus;
import com.wifirent.bd.domain.session.repository.RentalSessionRepository;
import com.wifirent.bd.domain.user.User;
import com.wifirent.bd.domain.user.UserRole;
import com.wifirent.bd.domain.user.repository.UserRepository;
import com.wifirent.bd.dto.PaymentCheckoutRequest;
import com.wifirent.bd.dto.PaymentDto;
import com.wifirent.bd.dto.PaymentWebhookRequest;
import com.wifirent.bd.exception.BadRequestException;
import com.wifirent.bd.exception.ForbiddenException;
import com.wifirent.bd.exception.ResourceNotFoundException;
import com.wifirent.bd.gateway.GatewayAdapter;
import com.wifirent.bd.domain.hotspot.GatewayDevice;
import com.wifirent.bd.domain.hotspot.repository.GatewayDeviceRepository;
import com.wifirent.bd.domain.packageplan.repository.PackagePlanRepository;
import com.wifirent.bd.domain.packageplan.PackagePlan;
import com.wifirent.bd.domain.packageplan.PackageType;
import com.wifirent.bd.payment.provider.PaymentProviderService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PaymentServiceTest {

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private RentalOrderRepository orderRepository;

    @Mock
    private RentalSessionRepository sessionRepository;

    @Mock
    private GatewayDeviceRepository gatewayDeviceRepository;

    @Mock
    private GatewayAdapter gatewayAdapter;

    @Mock
    private PaymentProviderService paymentProviderService;

    @InjectMocks
    private PaymentService paymentService;

    @Test
    void checkout_shouldCreatePendingPayment() {
        User customer = new User("Cust", "cust@example.com", "pass", UserRole.CUSTOMER);
        customer.setId(1L);

        RentalOrder order = new RentalOrder();
        order.setId(10L);
        order.setCustomer(customer);
        order.setStatus(RentalOrderStatus.PENDING);
        order.setTotalPriceMinorUnits(500L);

        when(orderRepository.findById(10L)).thenReturn(Optional.of(order));
        when(paymentRepository.findByIdempotencyKey("key-1")).thenReturn(Optional.empty());
        when(paymentProviderService.checkout(10L, 500L, "key-1"))
            .thenReturn(new PaymentProviderService.CheckoutResult("MOCK-123", null, true));
        when(paymentRepository.save(any(Payment.class))).thenAnswer(inv -> {
            Payment p = inv.getArgument(0);
            p.setId(100L);
            return p;
        });

        PaymentCheckoutRequest req = new PaymentCheckoutRequest(10L, "MOCK", "key-1");
        PaymentDto dto = paymentService.checkout(1L, req);

        assertEquals(100L, dto.id());
        assertEquals(PaymentStatus.PENDING.name(), dto.status());
        assertEquals(500L, dto.amountMinorUnits());
    }

    @Test
    void checkout_shouldThrowOnDuplicateIdempotency() {
        User customer = new User("Cust", "cust@example.com", "pass", UserRole.CUSTOMER);
        customer.setId(1L);

        RentalOrder order = new RentalOrder();
        order.setId(10L);
        order.setCustomer(customer);
        order.setStatus(RentalOrderStatus.PENDING);

        when(orderRepository.findById(10L)).thenReturn(Optional.of(order));
        when(paymentRepository.findByIdempotencyKey("key-1")).thenReturn(Optional.of(new Payment()));

        PaymentCheckoutRequest req = new PaymentCheckoutRequest(10L, "MOCK", "key-1");
        assertThrows(BadRequestException.class, () -> paymentService.checkout(1L, req));
    }

    @Test
    void webhook_shouldActivateOrderWhenCompleted() {
        User customer = new User("Cust", "cust@example.com", "pass", UserRole.CUSTOMER);
        customer.setId(1L);

        HotspotSetup setup = new HotspotSetup();
        RentalOrder order = setup.createPaidOrder(customer);

        Payment payment = new Payment();
        payment.setId(100L);
        payment.setRentalOrder(order);
        payment.setProvider(PaymentProvider.MOCK);
        payment.setStatus(PaymentStatus.PENDING);
        payment.setProviderTransactionRef("MOCK-123");
        payment.setAmountMinorUnits(500L);

        when(paymentRepository.findByProviderTransactionRef("MOCK-123")).thenReturn(Optional.of(payment));
        when(paymentProviderService.handleWebhook("MOCK-123", "COMPLETED", "key-1", "{}"))
            .thenReturn(new PaymentProviderService.WebhookResult(true, PaymentStatus.COMPLETED, null));
        when(paymentRepository.save(any())).thenReturn(payment);
        when(orderRepository.save(any())).thenReturn(order);
        when(gatewayDeviceRepository.findByHotspotId(10L)).thenReturn(List.of());
        when(sessionRepository.save(any())).thenAnswer(inv -> {
            RentalSession s = inv.getArgument(0);
            s.setId(200L);
            return s;
        });

        PaymentWebhookRequest req = new PaymentWebhookRequest("MOCK-123", "COMPLETED", "key-1", "{}");
        paymentService.handleWebhook("MOCK", req);

        assertEquals(RentalOrderStatus.PAID, order.getStatus());
    }

    @Test
    void webhook_shouldBeIdempotentWhenAlreadyCompleted() {
        User customer = new User("Cust", "cust@example.com", "pass", UserRole.CUSTOMER);
        customer.setId(1L);

        RentalOrder order = new RentalOrder();
        order.setId(10L);
        order.setCustomer(customer);
        order.setStatus(RentalOrderStatus.PAID);

        Payment payment = new Payment();
        payment.setId(100L);
        payment.setRentalOrder(order);
        payment.setProvider(PaymentProvider.MOCK);
        payment.setStatus(PaymentStatus.COMPLETED);
        payment.setProviderTransactionRef("MOCK-123");

        when(paymentRepository.findByProviderTransactionRef("MOCK-123")).thenReturn(Optional.of(payment));

        PaymentWebhookRequest req = new PaymentWebhookRequest("MOCK-123", "COMPLETED", "key-1", "{}");
        paymentService.handleWebhook("MOCK", req);

        assertEquals(PaymentStatus.COMPLETED, payment.getStatus());
    }

    @Test
    void webhook_shouldThrowWhenPaymentNotFound() {
        when(paymentRepository.findByProviderTransactionRef("UNKNOWN")).thenReturn(Optional.empty());
        when(paymentRepository.findByIdempotencyKey("key-1")).thenReturn(Optional.empty());

        PaymentWebhookRequest req = new PaymentWebhookRequest("UNKNOWN", "COMPLETED", "key-1", "{}");
        assertThrows(ResourceNotFoundException.class, () ->
            paymentService.handleWebhook("MOCK", req));
    }

    @Test
    void getPayment_shouldThrowWhenNotOwner() {
        User owner = new User("Host", "host@example.com", "pass", UserRole.HOST);
        owner.setId(99L);
        User customer = new User("Cust", "cust@example.com", "pass", UserRole.CUSTOMER);
        customer.setId(2L);

        HotspotSetup setup = new HotspotSetup();
        RentalOrder order = setup.createPaidOrder(customer);
        Payment payment = new Payment();
        payment.setId(100L);
        payment.setRentalOrder(order);
        payment.setStatus(PaymentStatus.COMPLETED);

        when(paymentRepository.findById(100L)).thenReturn(Optional.of(payment));

        assertThrows(ForbiddenException.class, () ->
            paymentService.getPayment(100L, owner));
    }

    static class HotspotSetup {
        RentalOrder createPaidOrder(User customer) {
            com.wifirent.bd.domain.user.HostProfile profile = new com.wifirent.bd.domain.user.HostProfile();
            User hostUser = new User("Host", "host@example.com", "pass", UserRole.HOST);
            hostUser.setId(1L);
            profile.setUser(hostUser);

            Hotspot hotspot = new Hotspot();
            hotspot.setId(10L);
            hotspot.setHost(profile);

            PackagePlan plan = new PackagePlan();
            plan.setId(100L);
            plan.setPriceMinorUnits(500L);
            plan.setActive(true);
            plan.setPackageType(PackageType.TIME_BASED);
            plan.setDurationMinutes(60);
            plan.setHotspot(hotspot);

            RentalOrder order = new RentalOrder();
            order.setId(10L);
            order.setCustomer(customer);
            order.setHotspot(hotspot);
            order.setPackagePlan(plan);
            order.setStatus(RentalOrderStatus.PAID);
            order.setTotalPriceMinorUnits(500L);
            return order;
        }
    }
}
