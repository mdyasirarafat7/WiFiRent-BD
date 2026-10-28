package com.wifirent.bd.domain.rental.repository;

import com.wifirent.bd.domain.rental.RentalOrder;
import com.wifirent.bd.domain.rental.RentalOrderStatus;
import com.wifirent.bd.domain.user.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface RentalOrderRepository extends JpaRepository<RentalOrder, Long> {
    Page<RentalOrder> findByCustomerId(Long customerId, Pageable pageable);

    @Query("SELECT o FROM RentalOrder o WHERE o.hotspot.host.user.id = :hostId")
    Page<RentalOrder> findByHostId(@Param("hostId") Long hostId, Pageable pageable);

    Optional<RentalOrder> findByIdempotencyKey(String idempotencyKey);
}
