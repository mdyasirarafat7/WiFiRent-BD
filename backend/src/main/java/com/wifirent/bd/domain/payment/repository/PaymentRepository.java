package com.wifirent.bd.domain.payment.repository;

import com.wifirent.bd.domain.payment.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {
    Optional<Payment> findByIdempotencyKey(String idempotencyKey);
    Optional<Payment> findByProviderTransactionRef(String providerTransactionRef);
    List<Payment> findByRentalOrderId(Long rentalOrderId);
}
