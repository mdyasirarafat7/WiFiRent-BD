package com.wifirent.bd.service;

import com.wifirent.bd.domain.session.RentalSession;
import com.wifirent.bd.domain.session.SessionStatus;
import com.wifirent.bd.domain.session.repository.RentalSessionRepository;
import com.wifirent.bd.domain.user.User;
import com.wifirent.bd.domain.user.UserRole;
import com.wifirent.bd.domain.user.repository.UserRepository;

import com.wifirent.bd.exception.BadRequestException;
import com.wifirent.bd.exception.ForbiddenException;
import com.wifirent.bd.exception.ResourceNotFoundException;
import java.time.Instant;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.wifirent.bd.dto.*;

@Service
public class SessionService {

    private final RentalSessionRepository sessionRepository;
    private final UserRepository userRepository;

    public SessionService(RentalSessionRepository sessionRepository, UserRepository userRepository) {
        this.sessionRepository = sessionRepository;
        this.userRepository = userRepository;
    }

    @Transactional(readOnly = true)
    public ActiveSessionResponse getActiveSession(Long customerId) {
        RentalSession session = sessionRepository.findByCustomerIdAndStatusOrderByStartedAtDesc(
            customerId, SessionStatus.ACTIVE).stream().findFirst().orElse(null);
        if (session == null) {
            return new ActiveSessionResponse(false, null);
        }
        // Enforce server-side expiry and quota check
        if (isSessionExpired(session)) {
            session.setStatus(SessionStatus.EXPIRED);
            sessionRepository.save(session);
            return new ActiveSessionResponse(false, null);
        }
        return new ActiveSessionResponse(true, toDto(session));
    }

    @Transactional(readOnly = true)
    public SessionDto getSession(Long id, User user) {
        RentalSession session = sessionRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Session not found"));
        checkAccess(session, user);
        return toDto(session);
    }

    @Transactional(readOnly = true)
    public Object listHistory(User user, Pageable pageable) {
        if (user.getRole() == UserRole.ADMIN) {
            return sessionRepository.findAll(pageable).map(this::toDto);
        }
        if (user.getRole() == UserRole.HOST) {
            return sessionRepository.findByHostId(user.getId(), pageable).map(this::toDto);
        }
        return sessionRepository.findByCustomerId(user.getId(), pageable).map(this::toDto);
    }

    @Transactional
    public void expireSession(Long sessionId) {
        RentalSession session = sessionRepository.findById(sessionId)
            .orElseThrow(() -> new ResourceNotFoundException("Session not found"));
        if (session.getStatus() == SessionStatus.ACTIVE) {
            session.setStatus(SessionStatus.EXPIRED);
            sessionRepository.save(session);
        }
    }

    @Transactional
    public void revokeSession(Long sessionId, User user) {
        RentalSession session = sessionRepository.findById(sessionId)
            .orElseThrow(() -> new ResourceNotFoundException("Session not found"));
        if (user.getRole() != UserRole.ADMIN &&
            !session.getHotspot().getHost().getUser().getId().equals(user.getId())) {
            throw new ForbiddenException("Not your session");
        }
        session.setStatus(SessionStatus.REVOKED);
        sessionRepository.save(session);
    }

    private void checkAccess(RentalSession session, User user) {
        if (user.getRole() == UserRole.ADMIN) return;
        if (user.getRole() == UserRole.HOST) {
            if (!session.getHotspot().getHost().getUser().getId().equals(user.getId())) {
                throw new ForbiddenException("Not your session");
            }
            return;
        }
        if (!session.getCustomer().getId().equals(user.getId())) {
            throw new ForbiddenException("Not your session");
        }
    }

    /**
     * Server-side enforcement: a session is expired if its expiration time has passed
     * or if its data quota has been fully consumed.
     */
    private boolean isSessionExpired(RentalSession session) {
        if (session.getExpiresAt() != null && session.getExpiresAt().isBefore(Instant.now())) {
            return true;
        }
        if (session.getDataQuotaBytes() != null
            && session.getConsumedBytes() != null
            && session.getConsumedBytes() >= session.getDataQuotaBytes()) {
            return true;
        }
        return false;
    }

    private SessionDto toDto(RentalSession s) {
        return new SessionDto(
            s.getId(), s.getCustomer().getId(), s.getHotspot().getId(),
            s.getPackagePlan().getId(), s.getRentalOrder().getId(),
            s.getStatus().name(), s.getStartedAt(), s.getExpiresAt(),
            s.getDataQuotaBytes(), s.getConsumedBytes(), s.getGatewaySessionId()
        );
    }
}



