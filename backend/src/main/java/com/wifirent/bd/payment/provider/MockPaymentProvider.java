package com.wifirent.bd.payment.provider;

import com.wifirent.bd.domain.payment.PaymentStatus;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Component;

@Component
public class MockPaymentProvider implements PaymentProviderService {

    private final Map<String, PaymentStatus> transactions = new ConcurrentHashMap<>();

    @Override
    public String providerName() {
        return "MOCK (simulated - not real money)";
    }

    @Override
    public CheckoutResult checkout(Long rentalOrderId, Long amountMinorUnits, String idempotencyKey) {
        String ref = "MOCK-" + Instant.now().toEpochMilli() + "-" + rentalOrderId;
        transactions.put(ref, PaymentStatus.PENDING);
        return new CheckoutResult(ref, null, true);
    }

    @Override
    public WebhookResult handleWebhook(String providerTransactionRef, String status,
                                       String idempotencyKey, String payload) {
        PaymentStatus ps;
        try {
            ps = PaymentStatus.valueOf(status.toUpperCase());
        } catch (IllegalArgumentException e) {
            return new WebhookResult(false, null, "Invalid status");
        }
        transactions.put(providerTransactionRef, ps);
        return new WebhookResult(true, ps, null);
    }

    public PaymentStatus getStatus(String ref) {
        return transactions.get(ref);
    }
}
