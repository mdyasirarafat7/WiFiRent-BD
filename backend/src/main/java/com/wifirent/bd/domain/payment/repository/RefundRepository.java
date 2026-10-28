package com.wifirent.bd.domain.payment.repository;

import com.wifirent.bd.domain.payment.Refund;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface RefundRepository extends JpaRepository<Refund, Long> {
    List<Refund> findByPaymentId(Long paymentId);
    List<Refund> findByRentalOrderId(Long rentalOrderId);
}
