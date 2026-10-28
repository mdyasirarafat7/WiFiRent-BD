package com.wifirent.bd.service;

import com.wifirent.bd.domain.hotspot.Hotspot;
import com.wifirent.bd.domain.hotspot.Hotspot.HotspotStatus;
import com.wifirent.bd.domain.hotspot.repository.HotspotRepository;
import com.wifirent.bd.domain.user.HostProfile;
import com.wifirent.bd.domain.user.User;
import com.wifirent.bd.domain.user.UserRole;
import com.wifirent.bd.domain.user.repository.HostProfileRepository;
import com.wifirent.bd.domain.user.repository.UserRepository;
import com.wifirent.bd.dto.CreateHotspotRequest;
import com.wifirent.bd.dto.HotspotDto;
import com.wifirent.bd.dto.NearbyHotspotRequest;
import com.wifirent.bd.dto.PagedHotspotResponse;
import com.wifirent.bd.exception.ForbiddenException;
import com.wifirent.bd.exception.ResourceNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import java.util.List;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class HotspotServiceTest {

    @Mock
    private HotspotRepository hotspotRepository;

    @Mock
    private HostProfileRepository hostProfileRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private HotspotService hotspotService;

    @Test
    void createHotspot_shouldCreateWhenHostApproved() {
        User hostUser = new User("Host", "host@example.com", "pass", UserRole.HOST);
        hostUser.setId(1L);
        HostProfile profile = new HostProfile();
        profile.setUser(hostUser);
        profile.setApproved(true);
        profile.setStatus(HostProfile.HostStatus.APPROVED);

        when(hostProfileRepository.findByUserId(1L)).thenReturn(Optional.of(profile));
        when(hotspotRepository.save(any(Hotspot.class))).thenAnswer(inv -> {
            Hotspot h = inv.getArgument(0);
            h.setId(10L);
            return h;
        });

        CreateHotspotRequest req = new CreateHotspotRequest(
            "My Hotspot", "desc", BigDecimal.valueOf(23.8103), BigDecimal.valueOf(90.4125), "MySSID", "Gulshan");

        HotspotDto dto = hotspotService.create(1L, req.name(), req.description(),
            req.latitude(), req.longitude(), req.ssid(), req.approximateArea());

        assertNotNull(dto);
        assertEquals("My Hotspot", dto.name());
        verify(hotspotRepository).save(any(Hotspot.class));
    }

    @Test
    void createHotspot_shouldThrowWhenHostNotApproved() {
        User hostUser = new User("Host", "host@example.com", "pass", UserRole.HOST);
        hostUser.setId(1L);
        HostProfile profile = new HostProfile();
        profile.setUser(hostUser);
        profile.setApproved(false);
        profile.setStatus(HostProfile.HostStatus.PENDING);

        when(hostProfileRepository.findByUserId(1L)).thenReturn(Optional.of(profile));

        assertThrows(ForbiddenException.class, () ->
            hotspotService.create(1L, "Name", "desc", BigDecimal.valueOf(23.8103), BigDecimal.valueOf(90.4125), "SSID", "Area"));
    }

    @Test
    void getById_shouldThrowWhenNotFound() {
        when(hotspotRepository.findById(999L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> hotspotService.getById(999L));
    }

    @Test
    void findNearby_shouldReturnPage() {
        User hostUser = new User("Host", "host@example.com", "pass", UserRole.HOST);
        hostUser.setId(1L);
        HostProfile profile = new HostProfile();
        profile.setUser(hostUser);

        Hotspot h = new Hotspot();
        h.setId(1L);
        h.setHost(profile);
        List<Hotspot> hotspotList = List.of(h);
        when(hotspotRepository.findAvailableNearby(
            23.8103, 90.4125, 5000.0, "ACTIVE"))
            .thenReturn(hotspotList);

        PagedHotspotResponse response = hotspotService.findNearby(
            23.8103, 90.4125, 5000.0);

        assertEquals(1, response.content().size());
        assertEquals(1L, response.content().get(0).id());
    }

    @Test
    void setAvailability_shouldThrowWhenNotOwner() {
        User owner = new User("Owner", "owner@example.com", "pass", UserRole.HOST);
        owner.setId(1L);
        User other = new User("Other", "other@example.com", "pass", UserRole.HOST);
        other.setId(2L);

        HostProfile profile = new HostProfile();
        profile.setUser(owner);

        Hotspot h = new Hotspot();
        h.setId(5L);
        h.setHost(profile);

        when(hotspotRepository.findById(5L)).thenReturn(Optional.of(h));

        assertThrows(ForbiddenException.class, () ->
            hotspotService.setAvailability(5L, 2L, false));
    }
}
