package com.wifirent.bd.service;

import com.wifirent.bd.domain.hotspot.Hotspot;
import com.wifirent.bd.domain.hotspot.Hotspot.HotspotStatus;
import com.wifirent.bd.domain.hotspot.repository.HotspotRepository;
import com.wifirent.bd.domain.packageplan.PackagePlan;
import com.wifirent.bd.domain.packageplan.repository.PackagePlanRepository;
import com.wifirent.bd.domain.rental.RentalOrder;
import com.wifirent.bd.domain.rental.RentalOrderStatus;
import com.wifirent.bd.domain.rental.repository.RentalOrderRepository;
import com.wifirent.bd.domain.session.RentalSession;
import com.wifirent.bd.domain.session.SessionStatus;
import com.wifirent.bd.domain.session.repository.RentalSessionRepository;
import com.wifirent.bd.domain.user.User;
import com.wifirent.bd.domain.user.UserRole;
import com.wifirent.bd.domain.user.repository.UserRepository;

import com.wifirent.bd.dto.CreateRentalOrderRequest;
import com.wifirent.bd.dto.CancelRentalRequest;
import com.wifirent.bd.dto.RentalOrderDto;
import com.wifirent.bd.exception.BadRequestException;
import com.wifirent.bd.exception.ForbiddenException;
import com.wifirent.bd.exception.ResourceNotFoundException;
import java.time.Instant;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.wifirent.bd.dto.*;

@Service
public class RentalService {

    private final RentalOrderRepository orderRepository;
    private final PackagePlanRepository packageRepository;
    private final HotspotRepository hotspotRepository;
    private final UserRepository userRepository;
    private final RentalSessionRepository sessionRepository;

    public RentalService(RentalOrderRepository orderRepository,
                         PackagePlanRepository packageRepository,
                         HotspotRepository hotspotRepository,
                         UserRepository userRepository,
                         RentalSessionRepository sessionRepository) {
        this.orderRepository = orderRepository;
        this.packageRepository = packageRepository;
        this.hotspotRepository = hotspotRepository;
        this.userRepository = userRepository;
        this.sessionRepository = sessionRepository;
    }

    @Transactional
    public RentalOrderDto createOrder(Long customerId,
                                                CreateRentalOrderRequest request) {
        if (request.idempotencyKey() != null) {
            if (orderRepository.findByIdempotencyKey(request.idempotencyKey()).isPresent()) {
                throw new BadRequestException("Duplicate order (idempotency key already used)");
            }
        }

        User customer = userRepository.findById(customerId)
            .orElseThrow(() -> new ResourceNotFoundException("Customer not found"));

        Hotspot hotspot = hotspotRepository.findById(request.hotspotId())
            .orElseThrow(() -> new ResourceNotFoundException("Hotspot not found"));
        if (!hotspot.getAvailable() || hotspot.getStatus() != HotspotStatus.ACTIVE) {
            throw new BadRequestException("Hotspot is not available");
        }

        PackagePlan plan = packageRepository.findById(request.packageId())
            .orElseThrow(() -> new ResourceNotFoundException("Package not found"));
        if (!plan.getHotspot().getId().equals(hotspot.getId())) {
            throw new BadRequestException("Package does not belong to this hotspot");
        }
        if (!plan.getActive()) {
            throw new BadRequestException("Package is not active");
        }

        RentalOrder order = new RentalOrder();
        order.setCustomer(customer);
        order.setHotspot(hotspot);
        order.setPackagePlan(plan);
        order.setStatus(RentalOrderStatus.PENDING);
        order.setTotalPriceMinorUnits(plan.getPriceMinorUnits());
        order.setIdempotencyKey(request.idempotencyKey() != null ? request.idempotencyKey() : UUID.randomUUID().toString());

        return toDto(orderRepository.save(order));
    }

    @Transactional(readOnly = true)
    public Object listOrders(User user, org.springframework.data.domain.Pageable pageable) {
        if (user.getRole() == UserRole.ADMIN) {
            return orderRepository.findAll(pageable).map(this::toDto);
        }
        if (user.getRole() == UserRole.HOST) {
            return orderRepository.findByHostId(user.getId(), pageable).map(this::toDto);
        }
        return orderRepository.findByCustomerId(user.getId(), pageable).map(this::toDto);
    }

    @Transactional(readOnly = true)
    public RentalOrderDto getOrder(Long id, User user) {
        RentalOrder order = orderRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Order not found"));
        checkOrderAccess(order, user);
        return toDto(order);
    }

    @Transactional
    public RentalOrderDto cancelOrder(Long id, User user, String reason) {
        RentalOrder order = orderRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Order not found"));
        checkOrderAccess(order, user);
        if (order.getStatus() != RentalOrderStatus.PENDING && order.getStatus() != RentalOrderStatus.PAID) {
            throw new BadRequestException("Order cannot be cancelled");
        }
        order.setStatus(RentalOrderStatus.CANCELLED);
        order.setCancelledAt(Instant.now());
        order.setCancellationReason(reason);
        return toDto(orderRepository.save(order));
    }

    private void checkOrderAccess(RentalOrder order, User user) {
        if (user.getRole() == UserRole.ADMIN) return;
        if (user.getRole() == UserRole.HOST) {
            if (!order.getHotspot().getHost().getUser().getId().equals(user.getId())) {
                throw new ForbiddenException("Not your order");
            }
            return;
        }
        if (!order.getCustomer().getId().equals(user.getId())) {
            throw new ForbiddenException("Not your order");
        }
    }

    private RentalOrderDto toDto(RentalOrder o) {
        return new RentalOrderDto(
            o.getId(), o.getCustomer().getId(), o.getHotspot().getId(),
            o.getPackagePlan().getId(), o.getStatus().name(),
            o.getTotalPriceMinorUnits(), "DEMO_CREDITS"
        );
    }
}
