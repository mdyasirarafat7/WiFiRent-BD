package com.wifirent.bd.service;

import com.wifirent.bd.domain.session.RentalSession;
import com.wifirent.bd.domain.session.SessionStatus;
import com.wifirent.bd.domain.session.repository.RentalSessionRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class SessionExpirationSchedulerTest {

    @Mock
    private RentalSessionRepository sessionRepository;

    @InjectMocks
    private SessionExpirationScheduler scheduler;

    @Test
    void expireSessions_shouldExpireActiveSessionsPastExpiration() {
        Instant past = Instant.now().minusSeconds(60);

        RentalSession expiredSession = new RentalSession();
        expiredSession.setId(1L);
        expiredSession.setStatus(SessionStatus.ACTIVE);
        expiredSession.setExpiresAt(past);

        when(sessionRepository.findByStatusAndExpiresAtBefore(eq(SessionStatus.ACTIVE), any()))
            .thenReturn(List.of(expiredSession));
        when(sessionRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        scheduler.expireSessions();

        assertEquals(SessionStatus.EXPIRED, expiredSession.getStatus());
    }

    @Test
    void expireSessions_shouldHandleEmptyList() {
        when(sessionRepository.findByStatusAndExpiresAtBefore(eq(SessionStatus.ACTIVE), any()))
            .thenReturn(List.of());

        assertDoesNotThrow(() -> scheduler.expireSessions());
    }
}
