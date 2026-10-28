package com.wifirent.bd.payment.provider;

import com.wifirent.bd.domain.payment.PaymentStatus;
import java.time.Instant;

public interface PaymentProviderService {

    String providerName();

    CheckoutResult checkout(Long rentalOrderId, Long amountMinorUnits, String idempotencyKey);

    WebhookResult handleWebhook(String providerTransactionRef, String status,
                                String idempotencyKey, String payload);

    class CheckoutResult {
        public final String providerTransactionRef;
        public final String checkoutUrl;
        public final boolean simulated;
        public CheckoutResult(String providerTransactionRef, String checkoutUrl, boolean simulated) {
            this.providerTransactionRef = providerTransactionRef;
            this.checkoutUrl = checkoutUrl;
            this.simulated = simulated;
        }
    }

    class WebhookResult {
        public final boolean processed;
        public final PaymentStatus status;
        public final String failureReason;
        public WebhookResult(boolean processed, PaymentStatus status, String failureReason) {
            this.processed = processed;
            this.status = status;
            this.failureReason = failureReason;
        }
    }
}
