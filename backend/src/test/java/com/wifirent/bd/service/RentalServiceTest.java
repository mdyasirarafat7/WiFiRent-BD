package com.wifirent.bd.service;

import com.wifirent.bd.domain.hotspot.Hotspot;
import com.wifirent.bd.domain.hotspot.Hotspot.HotspotStatus;
import com.wifirent.bd.domain.hotspot.repository.HotspotRepository;
import com.wifirent.bd.domain.packageplan.PackagePlan;
import com.wifirent.bd.domain.packageplan.PackageType;
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
import com.wifirent.bd.dto.RentalOrderDto;
import com.wifirent.bd.dto.CancelRentalRequest;
import com.wifirent.bd.exception.BadRequestException;
import com.wifirent.bd.exception.ForbiddenException;
import com.wifirent.bd.exception.ResourceNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RentalServiceTest {

    @Mock
    private RentalOrderRepository orderRepository;

    @Mock
    private PackagePlanRepository packageRepository;

    @Mock
    private HotspotRepository hotspotRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private RentalSessionRepository sessionRepository;

    @InjectMocks
    private RentalService rentalService;

    @Test
    void createOrder_shouldCreatePendingOrder() {
        User customer = new User("Cust", "cust@example.com", "pass", UserRole.CUSTOMER);
        customer.setId(1L);

        Hotspot hotspot = new Hotspot();
        hotspot.setId(10L);
        hotspot.setAvailable(true);
        hotspot.setStatus(HotspotStatus.ACTIVE);

        PackagePlan plan = new PackagePlan();
        plan.setId(100L);
        plan.setPriceMinorUnits(500L);
        plan.setActive(true);
        plan.setPackageType(PackageType.TIME_BASED);
        plan.setDurationMinutes(60);
        plan.setHotspot(hotspot);

        when(userRepository.findById(1L)).thenReturn(Optional.of(customer));
        when(hotspotRepository.findById(10L)).thenReturn(Optional.of(hotspot));
        when(packageRepository.findById(100L)).thenReturn(Optional.of(plan));
        when(orderRepository.findByIdempotencyKey("key-1")).thenReturn(Optional.empty());
        when(orderRepository.save(any(RentalOrder.class))).thenAnswer(inv -> {
            RentalOrder o = inv.getArgument(0);
            o.setId(1000L);
            return o;
        });

        CreateRentalOrderRequest req = new CreateRentalOrderRequest(10L, 100L, "key-1");
        RentalOrderDto dto = rentalService.createOrder(1L, req);

        assertEquals(1000L, dto.id());
        assertEquals(RentalOrderStatus.PENDING.name(), dto.status());
    }

    @Test
    void createOrder_shouldThrowOnDuplicateIdempotencyKey() {
        when(orderRepository.findByIdempotencyKey("key-1")).thenReturn(Optional.of(new RentalOrder()));

        CreateRentalOrderRequest req = new CreateRentalOrderRequest(10L, 100L, "key-1");
        assertThrows(BadRequestException.class, () -> rentalService.createOrder(1L, req));
    }

    @Test
    void createOrder_shouldThrowWhenHotspotNotAvailable() {
        User customer = new User("Cust", "cust@example.com", "pass", UserRole.CUSTOMER);
        customer.setId(1L);

        Hotspot hotspot = new Hotspot();
        hotspot.setId(10L);
        hotspot.setAvailable(false);

        when(userRepository.findById(1L)).thenReturn(Optional.of(customer));
        when(hotspotRepository.findById(10L)).thenReturn(Optional.of(hotspot));

        CreateRentalOrderRequest req = new CreateRentalOrderRequest(10L, 100L, "key-1");
        assertThrows(BadRequestException.class, () -> rentalService.createOrder(1L, req));
    }

    @Test
    void cancelOrder_shouldThrowWhenNotOwner() {
        User owner = new User("Owner", "owner@example.com", "pass", UserRole.HOST);
        owner.setId(1L);
        User other = new User("Other", "other@example.com", "pass", UserRole.HOST);
        other.setId(2L);

        Hotspot hotspot = new Hotspot();
        com.wifirent.bd.domain.user.HostProfile profile = new com.wifirent.bd.domain.user.HostProfile();
        profile.setUser(owner);
        hotspot.setHost(profile);

        RentalOrder order = new RentalOrder();
        order.setId(50L);
        order.setCustomer(customer());
        order.setHotspot(hotspot);
        order.setStatus(RentalOrderStatus.PENDING);

        when(orderRepository.findById(50L)).thenReturn(Optional.of(order));

        assertThrows(ForbiddenException.class, () ->
            rentalService.cancelOrder(50L, other, "test"));
    }

    @Test
    void cancelOrder_shouldSucceedWhenOwner() {
        User customer = new User("Cust", "cust@example.com", "pass", UserRole.CUSTOMER);
        customer.setId(1L);

        Hotspot hotspot = new Hotspot();
        hotspot.setId(10L);

        PackagePlan plan = new PackagePlan();
        plan.setId(100L);
        plan.setPriceMinorUnits(500L);
        plan.setHotspot(hotspot);

        RentalOrder order = new RentalOrder();
        order.setId(50L);
        order.setCustomer(customer);
        order.setHotspot(hotspot);
        order.setPackagePlan(plan);
        order.setStatus(RentalOrderStatus.PENDING);

        when(orderRepository.findById(50L)).thenReturn(Optional.of(order));
        when(orderRepository.save(any())).thenReturn(order);

        CancelRentalRequest req = new CancelRentalRequest("changed mind");
        RentalOrderDto dto = rentalService.cancelOrder(50L, customer, "changed mind");

        assertEquals(RentalOrderStatus.CANCELLED.name(), dto.status());
    }

    private User customer() {
        User u = new User("Cust", "cust@example.com", "pass", UserRole.CUSTOMER);
        u.setId(1L);
        return u;
    }
}
