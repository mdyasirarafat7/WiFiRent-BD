package com.wifirent.bd.service;

import com.wifirent.bd.domain.audit.AuditLog;
import com.wifirent.bd.domain.audit.AuditAction;
import com.wifirent.bd.domain.audit.repository.AuditLogRepository;
import com.wifirent.bd.domain.hotspot.Hotspot;
import com.wifirent.bd.domain.hotspot.Hotspot.HotspotStatus;
import com.wifirent.bd.domain.hotspot.repository.HotspotRepository;
import com.wifirent.bd.domain.payment.Payment;
import com.wifirent.bd.domain.payment.repository.PaymentRepository;
import com.wifirent.bd.domain.session.RentalSession;
import com.wifirent.bd.domain.session.repository.RentalSessionRepository;
import com.wifirent.bd.domain.user.User;
import com.wifirent.bd.domain.user.UserRole;
import com.wifirent.bd.domain.user.repository.UserRepository;

import com.wifirent.bd.dto.PagedHotspotResponse;
import com.wifirent.bd.exception.ResourceNotFoundException;
import java.time.Instant;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.wifirent.bd.dto.*;

@Service
public class AdminService {

    private final UserRepository userRepository;
    private final HotspotRepository hotspotRepository;
    private final PaymentRepository paymentRepository;
    private final RentalSessionRepository sessionRepository;
    private final AuditLogRepository auditLogRepository;

    public AdminService(UserRepository userRepository,
                        HotspotRepository hotspotRepository,
                        PaymentRepository paymentRepository,
                        RentalSessionRepository sessionRepository,
                        AuditLogRepository auditLogRepository) {
        this.userRepository = userRepository;
        this.hotspotRepository = hotspotRepository;
        this.paymentRepository = paymentRepository;
        this.sessionRepository = sessionRepository;
        this.auditLogRepository = auditLogRepository;
    }

    @Transactional(readOnly = true)
    public Object listUsers(Pageable pageable) {
        return userRepository.findAll(pageable).map(this::toUserDto);
    }

    @Transactional(readOnly = true)
    public PagedHotspotResponse listHotspots(Pageable pageable) {
        Page<Hotspot> page = hotspotRepository.findAll(pageable);
        return new PagedHotspotResponse(
            page.getContent().stream().map(this::toHotspotDto).toList(),
            page.getNumber(), page.getSize(), page.getTotalElements(), page.getTotalPages());
    }

    @Transactional
    public HotspotDto setHotspotStatus(Long id, String status) {
        Hotspot h = hotspotRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Hotspot not found"));
        try {
            h.setStatus(HotspotStatus.valueOf(status.toUpperCase()));
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid status");
        }
        return toHotspotDto(hotspotRepository.save(h));
    }

    @Transactional(readOnly = true)
    public Object listPayments(Pageable pageable) {
        return paymentRepository.findAll(pageable).map(this::toPaymentDto);
    }

    @Transactional(readOnly = true)
    public Object listSessions(Pageable pageable) {
        return sessionRepository.findAll(pageable).map(this::toSessionDto);
    }

    @Transactional(readOnly = true)
    public Object listAuditLogs(Pageable pageable, Long adminUserId) {
        return auditLogRepository.findAllByOrderByCreatedAtDesc(pageable);
    }

    @Transactional
    public void log(Actor actor, AuditAction action, String entityType, String entityId, String details, String ip) {
        AuditLog log = new AuditLog();
        log.setActorUserId(actor.userId);
        log.setActorRole(actor.role);
        log.setAction(action);
        log.setEntityType(entityType);
        log.setEntityId(entityId);
        log.setDetails(details);
        log.setIpAddress(ip);
        auditLogRepository.save(log);
    }

    public record Actor(Long userId, String role) {}

    private Object toUserDto(User u) {
        return new java.util.LinkedHashMap<String, Object>() {{
            put("id", u.getId()); put("name", u.getName()); put("email", u.getEmail());
            put("role", u.getRole()); put("enabled", u.getEnabled());
        }};
    }

    private HotspotDto toHotspotDto(Hotspot h) {
        return new HotspotDto(h.getId(), h.getName(), h.getDescription(), h.getLatitude(),
            h.getLongitude(), h.getSsid(), h.getApproximateArea(), h.getAvailable(),
            h.getStatus().name(), h.getHost().getUser().getId(), h.getHost().getBusinessName());
    }

    private Object toPaymentDto(Payment p) {
        return new java.util.LinkedHashMap<String, Object>() {{
            put("id", p.getId()); put("rentalOrderId", p.getRentalOrder().getId());
            put("provider", p.getProvider()); put("status", p.getStatus());
            put("amountMinorUnits", p.getAmountMinorUnits());
        }};
    }

    private Object toSessionDto(RentalSession s) {
        return new java.util.LinkedHashMap<String, Object>() {{
            put("id", s.getId()); put("customerId", s.getCustomer().getId());
            put("status", s.getStatus()); put("startedAt", s.getStartedAt());
            put("expiresAt", s.getExpiresAt());
        }};
    }
}


