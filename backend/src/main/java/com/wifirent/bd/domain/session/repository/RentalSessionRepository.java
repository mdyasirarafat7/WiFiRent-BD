package com.wifirent.bd.domain.session.repository;

import com.wifirent.bd.domain.session.RentalSession;
import com.wifirent.bd.domain.session.SessionStatus;
import com.wifirent.bd.domain.user.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Repository
public interface RentalSessionRepository extends JpaRepository<RentalSession, Long> {
    Page<RentalSession> findByCustomerId(Long customerId, Pageable pageable);

    @Query("SELECT s FROM RentalSession s WHERE s.hotspot.host.user.id = :hostId")
    Page<RentalSession> findByHostId(@Param("hostId") Long hostId, Pageable pageable);

    List<RentalSession> findByStatusAndExpiresAtBefore(SessionStatus status, Instant before);

    List<RentalSession> findByCustomerIdAndStatusOrderByStartedAtDesc(Long customerId, SessionStatus status);

    Optional<RentalSession> findByGatewaySessionId(String gatewaySessionId);
}
