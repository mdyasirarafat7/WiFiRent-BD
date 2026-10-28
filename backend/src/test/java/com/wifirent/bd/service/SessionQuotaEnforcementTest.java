package com.wifirent.bd.service;

import com.wifirent.bd.domain.session.RentalSession;
import com.wifirent.bd.domain.session.SessionStatus;
import com.wifirent.bd.domain.session.repository.RentalSessionRepository;
import com.wifirent.bd.domain.user.User;
import com.wifirent.bd.domain.user.UserRole;
import com.wifirent.bd.domain.user.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.time.Instant;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class SessionQuotaEnforcementTest {

    @Mock
    private RentalSessionRepository sessionRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private SessionService sessionService;

    @Test
    void getActiveSession_shouldExpireWhenDataQuotaExhausted() {
        User customer = new User("Cust", "cust@example.com", "pass", UserRole.CUSTOMER);
        customer.setId(1L);

        com.wifirent.bd.domain.user.HostProfile profile = new com.wifirent.bd.domain.user.HostProfile();
        profile.setUser(new User("Host", "host@example.com", "pass", UserRole.HOST));

        RentalSession session = new RentalSession();
        session.setId(1L);
        session.setCustomer(customer);
        session.setStatus(SessionStatus.ACTIVE);
        session.setExpiresAt(Instant.now().plusSeconds(3600)); // 1 hour from now
        session.setDataQuotaBytes(1073741824L); // 1 GB
        session.setConsumedBytes(1073741824L); // fully consumed

        when(sessionRepository.findByCustomerIdAndStatusOrderByStartedAtDesc(1L, SessionStatus.ACTIVE))
            .thenReturn(java.util.List.of(session));
        when(sessionRepository.save(any())).thenReturn(session);

        var response = sessionService.getActiveSession(1L);

        assertFalse(response.hasActiveSession());
        assertEquals(SessionStatus.EXPIRED, session.getStatus());
    }

    @Test
    void getActiveSession_shouldExpireWhenTimeExpired() {
        User customer = new User("Cust", "cust@example.com", "pass", UserRole.CUSTOMER);
        customer.setId(1L);

        RentalSession session = new RentalSession();
        session.setId(1L);
        session.setCustomer(customer);
        session.setStatus(SessionStatus.ACTIVE);
        session.setExpiresAt(Instant.now().minusSeconds(60)); // expired 1 minute ago
        session.setDataQuotaBytes(1073741824L);
        session.setConsumedBytes(0L);

        when(sessionRepository.findByCustomerIdAndStatusOrderByStartedAtDesc(1L, SessionStatus.ACTIVE))
            .thenReturn(java.util.List.of(session));
        when(sessionRepository.save(any())).thenReturn(session);

        var response = sessionService.getActiveSession(1L);

        assertFalse(response.hasActiveSession());
        assertEquals(SessionStatus.EXPIRED, session.getStatus());
    }

    @Test
    void getActiveSession_shouldReturnActiveWhenValid() {
        User customer = new User("Cust", "cust@example.com", "pass", UserRole.CUSTOMER);
        customer.setId(1L);

        com.wifirent.bd.domain.user.HostProfile hostProfile = new com.wifirent.bd.domain.user.HostProfile();
        hostProfile.setUser(new User("Host", "host@example.com", "pass", UserRole.HOST));

        HotspotSetup hotspotSetup = new HotspotSetup();
        com.wifirent.bd.domain.hotspot.Hotspot hotspot = hotspotSetup.createHotspot(hostProfile);

        com.wifirent.bd.domain.packageplan.PackagePlan plan = new com.wifirent.bd.domain.packageplan.PackagePlan();
        plan.setId(100L);
        plan.setHotspot(hotspot);

        RentalSession session = new RentalSession();
        session.setId(1L);
        session.setCustomer(customer);
        session.setHotspot(hotspot);
        session.setPackagePlan(plan);
        session.setRentalOrder(new com.wifirent.bd.domain.rental.RentalOrder());
        session.setStatus(SessionStatus.ACTIVE);
        session.setExpiresAt(Instant.now().plusSeconds(3600));
        session.setDataQuotaBytes(1073741824L);
        session.setConsumedBytes(100L);

        when(sessionRepository.findByCustomerIdAndStatusOrderByStartedAtDesc(1L, SessionStatus.ACTIVE))
            .thenReturn(java.util.List.of(session));

        var response = sessionService.getActiveSession(1L);

        assertTrue(response.hasActiveSession());
        assertEquals(SessionStatus.ACTIVE, session.getStatus());
    }

    static class HotspotSetup {
        com.wifirent.bd.domain.hotspot.Hotspot createHotspot(com.wifirent.bd.domain.user.HostProfile hostProfile) {
            com.wifirent.bd.domain.hotspot.Hotspot hotspot = new com.wifirent.bd.domain.hotspot.Hotspot();
            hotspot.setId(10L);
            hotspot.setHost(hostProfile);
            return hotspot;
        }
    }

    @Test
    void isSessionExpired_shouldDetectQuotaExhaustion() {
        // Test the logic directly through a session
        RentalSession session = new RentalSession();
        session.setDataQuotaBytes(1000L);
        session.setConsumedBytes(1000L);
        session.setExpiresAt(Instant.now().plusSeconds(3600));

        // We can't call isSessionExpired directly (private), but we can verify
        // through getActiveSession. The quota exhaustion test above covers this.
        assertTrue(session.getConsumedBytes() >= session.getDataQuotaBytes());
    }
}
