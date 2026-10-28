package com.wifirent.bd.service;

import com.wifirent.bd.domain.packageplan.PackagePlan;
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

import com.wifirent.bd.exception.BadRequestException;
import com.wifirent.bd.exception.ForbiddenException;
import com.wifirent.bd.exception.ResourceNotFoundException;
import com.wifirent.bd.gateway.GatewayAdapter;
import com.wifirent.bd.gateway.GatewayAdapter.GatewaySessionResult;
import com.wifirent.bd.domain.hotspot.GatewayDevice;
import com.wifirent.bd.domain.hotspot.repository.GatewayDeviceRepository;
import com.wifirent.bd.payment.provider.PaymentProviderService;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import com.wifirent.bd.dto.*;

@Service
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final RentalOrderRepository orderRepository;
    private final RentalSessionRepository sessionRepository;
    private final GatewayDeviceRepository gatewayDeviceRepository;
    private final GatewayAdapter gatewayAdapter;
    private final PaymentProviderService paymentProviderService;

    public PaymentService(PaymentRepository paymentRepository,
                          RentalOrderRepository orderRepository,
                          RentalSessionRepository sessionRepository,
                          GatewayDeviceRepository gatewayDeviceRepository,
                          GatewayAdapter gatewayAdapter,
                          PaymentProviderService paymentProviderService) {
        this.paymentRepository = paymentRepository;
        this.orderRepository = orderRepository;
        this.sessionRepository = sessionRepository;
        this.gatewayDeviceRepository = gatewayDeviceRepository;
        this.gatewayAdapter = gatewayAdapter;
        this.paymentProviderService = paymentProviderService;
    }

    @Transactional
    public PaymentDto checkout(Long userId,
                                          PaymentCheckoutRequest request) {
        RentalOrder order = orderRepository.findById(request.rentalOrderId())
            .orElseThrow(() -> new ResourceNotFoundException("Order not found"));
        if (!order.getCustomer().getId().equals(userId)) {
            throw new ForbiddenException("Not your order");
        }
        if (order.getStatus() != RentalOrderStatus.PENDING) {
            throw new BadRequestException("Order is not pending");
        }

        if (paymentRepository.findByIdempotencyKey(request.idempotencyKey() != null ? request.idempotencyKey() : "").isPresent()) {
            throw new BadRequestException("Duplicate payment (idempotency key already used)");
        }

        PaymentProvider provider;
        try {
            provider = PaymentProvider.valueOf(request.provider().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new BadRequestException("Unsupported payment provider");
        }

        Payment payment = new Payment();
        payment.setRentalOrder(order);
        payment.setProvider(provider);
        payment.setStatus(PaymentStatus.PENDING);
        payment.setAmountMinorUnits(order.getTotalPriceMinorUnits());
        payment.setCurrency("BDT");
        payment.setIdempotencyKey(request.idempotencyKey() != null ? request.idempotencyKey() : UUID.randomUUID().toString());

        PaymentProviderService.CheckoutResult checkout = paymentProviderService.checkout(
            order.getId(), order.getTotalPriceMinorUnits(), payment.getIdempotencyKey());
        payment.setProviderTransactionRef(checkout.providerTransactionRef);

        Payment saved = paymentRepository.save(payment);

        return toDto(saved);
    }

    @Transactional
    public void handleWebhook(String provider, PaymentWebhookRequest request) {
        PaymentProvider providerEnum;
        try {
            providerEnum = PaymentProvider.valueOf(provider.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new BadRequestException("Unsupported provider");
        }

        if (paymentRepository.findByProviderTransactionRef(request.providerTransactionRef()).isEmpty() &&
            paymentRepository.findByIdempotencyKey(request.idempotencyKey()).isEmpty()) {
            throw new ResourceNotFoundException("Payment not found");
        }

        Payment payment = paymentRepository.findByProviderTransactionRef(request.providerTransactionRef())
            .orElseGet(() -> paymentRepository.findByIdempotencyKey(request.idempotencyKey())
                .orElseThrow(() -> new ResourceNotFoundException("Payment not found")));

        if (payment.getStatus() == PaymentStatus.COMPLETED) {
            return; // idempotent - already processed
        }

        PaymentProviderService.WebhookResult result = paymentProviderService.handleWebhook(
            request.providerTransactionRef(), request.status(), request.idempotencyKey(), request.payload());

        payment.setStatus(result.status);
        payment.setCallbackReceivedAt(Instant.now());
        payment.setCallbackPayload(request.payload());
        paymentRepository.save(payment);

        if (result.status == PaymentStatus.COMPLETED) {
            RentalOrder order = payment.getRentalOrder();
            order.setStatus(RentalOrderStatus.PAID);
            orderRepository.save(order);

            try {
                createSessionAndAuthorize(order, payment);
            } catch (Exception e) {
                // Session activation failed but payment is completed.
                // Session remains PENDING/FAILED for retry; payment stays COMPLETED.
            }
        }
    }

    @Transactional(readOnly = true)
    public PaymentDto getPayment(Long id, User user) {
        Payment payment = paymentRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Payment not found"));
        checkAccess(payment, user);
        return toDto(payment);
    }

    private void checkAccess(Payment payment, User user) {
        if (user.getRole() == UserRole.ADMIN) return;
        if (user.getRole() == UserRole.HOST) {
            if (!payment.getRentalOrder().getHotspot().getHost().getUser().getId().equals(user.getId())) {
                throw new ForbiddenException("Not your payment");
            }
            return;
        }
        if (!payment.getRentalOrder().getCustomer().getId().equals(user.getId())) {
            throw new ForbiddenException("Not your payment");
        }
    }

    private void createSessionAndAuthorize(RentalOrder order, Payment payment) {
        PackagePlan plan = order.getPackagePlan();
        Instant now = Instant.now();

        Integer durationMinutes = plan.getDurationMinutes();
        Long dataQuotaBytes = plan.getDataQuotaBytes();
        Instant expiresAt = null;
        if (plan.getValidityMinutes() != null) {
            expiresAt = now.plus(plan.getValidityMinutes(), ChronoUnit.MINUTES);
        } else if (durationMinutes != null) {
            expiresAt = now.plus(durationMinutes, ChronoUnit.MINUTES);
        }

        RentalSession session = new RentalSession();
        session.setCustomer(order.getCustomer());
        session.setHotspot(order.getHotspot());
        session.setPackagePlan(plan);
        session.setRentalOrder(order);
        session.setStartedAt(now);
        session.setExpiresAt(expiresAt);
        session.setDataQuotaBytes(dataQuotaBytes);
        session.setConsumedBytes(0L);
        session.setStatus(SessionStatus.PENDING);
        session.setGatewayDeviceId(order.getHotspot().getId().toString());
        session = sessionRepository.save(session);

        String gatewaySessionId = "sess-" + session.getId();
        session.setGatewaySessionId(gatewaySessionId);
        sessionRepository.save(session);

        GatewayDevice device = gatewayDeviceRepository.findByHotspotId(order.getHotspot().getId()).stream()
            .findFirst().orElse(null);
        if (device != null) {
            GatewaySessionResult result = gatewayAdapter.authorizeSession(
                device, gatewaySessionId, durationMinutes, dataQuotaBytes, plan.getSpeedLimitKbps());
            if (!result.authorized) {
                session.setStatus(SessionStatus.FAILED);
                sessionRepository.save(session);
                return;
            }
        }

        session.setStatus(SessionStatus.ACTIVE);
        sessionRepository.save(session);
    }

    private PaymentDto toDto(Payment p) {
        return new PaymentDto(
            p.getId(), p.getRentalOrder().getId(), p.getProvider().name(),
            p.getStatus().name(), p.getAmountMinorUnits(), p.getCurrency(),
            p.getProviderTransactionRef()
        );
    }
}




