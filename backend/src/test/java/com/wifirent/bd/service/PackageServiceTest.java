package com.wifirent.bd.service;

import com.wifirent.bd.domain.hotspot.Hotspot;
import com.wifirent.bd.domain.packageplan.PackagePlan;
import com.wifirent.bd.domain.packageplan.PackageType;
import com.wifirent.bd.domain.packageplan.repository.PackagePlanRepository;
import com.wifirent.bd.domain.hotspot.repository.HotspotRepository;
import com.wifirent.bd.domain.user.HostProfile;
import com.wifirent.bd.domain.user.User;
import com.wifirent.bd.domain.user.UserRole;
import com.wifirent.bd.domain.user.repository.HostProfileRepository;
import com.wifirent.bd.domain.user.repository.UserRepository;
import com.wifirent.bd.dto.CreatePackageRequest;
import com.wifirent.bd.dto.PackagePlanDto;
import com.wifirent.bd.dto.UpdatePackageRequest;
import com.wifirent.bd.exception.BadRequestException;
import com.wifirent.bd.exception.ForbiddenException;
import com.wifirent.bd.exception.ResourceNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PackageServiceTest {

    @Mock
    private PackagePlanRepository packageRepository;

    @Mock
    private HotspotRepository hotspotRepository;

    @Mock
    private HostProfileRepository hostProfileRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private PackageService packageService;

    @Test
    void createTimeBasedPackage_shouldSucceed() {
        User hostUser = new User("Host", "host@example.com", "pass", UserRole.HOST);
        hostUser.setId(1L);
        HostProfile profile = new HostProfile();
        profile.setUser(hostUser);

        Hotspot hotspot = new Hotspot();
        hotspot.setId(10L);
        hotspot.setHost(profile);

        when(hotspotRepository.findById(10L)).thenReturn(Optional.of(hotspot));
        when(packageRepository.save(any(PackagePlan.class))).thenAnswer(inv -> {
            PackagePlan p = inv.getArgument(0);
            p.setId(100L);
            return p;
        });

        CreatePackageRequest req = new CreatePackageRequest(500L, "TIME_BASED", 60, null, null, null);
        PackagePlanDto dto = packageService.create(10L, 1L, req);

        assertEquals(100L, dto.id());
        assertEquals(500L, dto.priceMinorUnits());
        assertEquals("TIME_BASED", dto.packageType());
        assertEquals(60, dto.durationMinutes());
    }

    @Test
    void createDataBasedPackage_shouldSucceed() {
        User hostUser = new User("Host", "host@example.com", "pass", UserRole.HOST);
        hostUser.setId(1L);
        HostProfile profile = new HostProfile();
        profile.setUser(hostUser);

        Hotspot hotspot = new Hotspot();
        hotspot.setId(10L);
        hotspot.setHost(profile);

        when(hotspotRepository.findById(10L)).thenReturn(Optional.of(hotspot));
        when(packageRepository.save(any(PackagePlan.class))).thenAnswer(inv -> {
            PackagePlan p = inv.getArgument(0);
            p.setId(100L);
            return p;
        });

        CreatePackageRequest req = new CreatePackageRequest(1000L, "DATA_BASED", null, 1073741824L, null, null);
        PackagePlanDto dto = packageService.create(10L, 1L, req);

        assertEquals(1000L, dto.priceMinorUnits());
        assertEquals("DATA_BASED", dto.packageType());
        assertEquals(1073741824L, dto.dataQuotaBytes());
    }

    @Test
    void createTimeBasedWithoutDuration_shouldThrow() {
        User hostUser = new User("Host", "host@example.com", "pass", UserRole.HOST);
        hostUser.setId(1L);
        HostProfile profile = new HostProfile();
        profile.setUser(hostUser);

        Hotspot hotspot = new Hotspot();
        hotspot.setId(10L);
        hotspot.setHost(profile);

        CreatePackageRequest req = new CreatePackageRequest(500L, "TIME_BASED", null, null, null, null);
        assertThrows(BadRequestException.class, () ->
            packageService.create(10L, 1L, req));
    }

    @Test
    void update_shouldThrowWhenNotOwner() {
        User owner = new User("Owner", "owner@example.com", "pass", UserRole.HOST);
        owner.setId(1L);
        User other = new User("Other", "other@example.com", "pass", UserRole.HOST);
        other.setId(2L);

        HostProfile profile = new HostProfile();
        profile.setUser(owner);

        Hotspot hotspot = new Hotspot();
        hotspot.setId(10L);
        hotspot.setHost(profile);

        PackagePlan plan = new PackagePlan();
        plan.setId(100L);
        plan.setHotspot(hotspot);

        when(packageRepository.findById(100L)).thenReturn(Optional.of(plan));

        UpdatePackageRequest req = new UpdatePackageRequest(600L, null, null, null, null, null, null);
        assertThrows(ForbiddenException.class, () ->
            packageService.update(100L, 2L, req));
    }

    @Test
    void deactivate_shouldSetActiveFalse() {
        PackagePlan plan = new PackagePlan();
        plan.setId(100L);
        plan.setActive(true);
        when(packageRepository.findById(100L)).thenReturn(Optional.of(plan));
        when(packageRepository.save(any())).thenReturn(plan);

        packageService.deactivate(100L);

        assertFalse(plan.getActive());
        verify(packageRepository).save(plan);
    }
}
