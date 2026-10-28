package com.wifirent.bd.service;

import com.wifirent.bd.domain.session.RentalSession;
import com.wifirent.bd.domain.session.SessionStatus;
import com.wifirent.bd.domain.session.repository.RentalSessionRepository;
import java.time.Instant;
import java.util.List;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SessionExpirationScheduler {

    private final RentalSessionRepository sessionRepository;

    public SessionExpirationScheduler(RentalSessionRepository sessionRepository) {
        this.sessionRepository = sessionRepository;
    }

    @Scheduled(fixedDelayString = "${app.scheduler.expiration-delay-ms:60000}")
    @Transactional
    public void expireSessions() {
        Instant now = Instant.now();
        List<RentalSession> expired = sessionRepository.findByStatusAndExpiresAtBefore(
            SessionStatus.ACTIVE, now);
        for (RentalSession session : expired) {
            session.setStatus(SessionStatus.EXPIRED);
            sessionRepository.save(session);
        }
    }
}
