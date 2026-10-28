package com.wifirent.bd.service;

import com.wifirent.bd.domain.hotspot.Hotspot;
import com.wifirent.bd.domain.hotspot.Hotspot.HotspotStatus;
import com.wifirent.bd.domain.hotspot.repository.HotspotRepository;
import com.wifirent.bd.domain.user.HostProfile;
import com.wifirent.bd.domain.user.repository.HostProfileRepository;
import com.wifirent.bd.domain.user.repository.UserRepository;

import com.wifirent.bd.dto.PagedHotspotResponse;
import com.wifirent.bd.dto.NearbyHotspotRequest;
import com.wifirent.bd.exception.BadRequestException;
import com.wifirent.bd.exception.ForbiddenException;
import com.wifirent.bd.exception.ResourceNotFoundException;
import jakarta.validation.Valid;
import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.wifirent.bd.dto.*;
import org.springframework.validation.annotation.Validated;

@Service
@Validated
public class HotspotService {

    private final HotspotRepository hotspotRepository;
    private final HostProfileRepository hostProfileRepository;
    private final UserRepository userRepository;

    public HotspotService(HotspotRepository hotspotRepository,
                          HostProfileRepository hostProfileRepository,
                          UserRepository userRepository) {
        this.hotspotRepository = hotspotRepository;
        this.hostProfileRepository = hostProfileRepository;
        this.userRepository = userRepository;
    }

    @Transactional(readOnly = true)
    public PagedHotspotResponse listAvailable(Pageable pageable) {
        Page<Hotspot> page = hotspotRepository.findByStatusAndAvailableOrderByCreatedAtDesc(
            HotspotStatus.ACTIVE, true, pageable);
        return map(page);
    }

    @Transactional(readOnly = true)
    public HotspotDto getById(Long id) {
        Hotspot h = hotspotRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Hotspot not found"));
        return toDto(h);
    }

    @Transactional(readOnly = true)
    public PagedHotspotResponse findNearby(Double latitude, Double longitude, Double radiusMeters) {
        if (latitude == null || longitude == null || radiusMeters == null) {
            throw new BadRequestException("lat, lng, and radiusMeters are required");
        }
        List<Hotspot> list = hotspotRepository.findAvailableNearby(
            latitude, longitude, radiusMeters, HotspotStatus.ACTIVE.name());
        return new PagedHotspotResponse(
            list.stream().map(this::toDto).toList(),
            0, list.size(), (long) list.size(), 1
        );
    }

    @Transactional
    public HotspotDto create(Long hostId, String name, String description,
                             BigDecimal latitude, BigDecimal longitude, String ssid, String approximateArea) {
        HostProfile host = hostProfileRepository.findByUserId(hostId)
            .orElseThrow(() -> new ResourceNotFoundException("Host profile not found"));
        if (!HostProfile.HostStatus.APPROVED.equals(host.getStatus()) && !host.getApproved()) {
            throw new ForbiddenException("Host profile not approved");
        }

        Hotspot hotspot = new Hotspot();
        hotspot.setName(name);
        hotspot.setDescription(description);
        hotspot.setLatitude(latitude);
        hotspot.setLongitude(longitude);
        hotspot.setSsid(ssid);
        hotspot.setApproximateArea(approximateArea);
        hotspot.setAvailable(true);
        hotspot.setStatus(HotspotStatus.ACTIVE);
        hotspot.setHost(host);

        return toDto(hotspotRepository.save(hotspot));
    }

    @Transactional
    public HotspotDto update(Long hotspotId, Long hostId, String name, String description,
                             String ssid, String approximateArea) {
        Hotspot h = hotspotRepository.findById(hotspotId)
            .orElseThrow(() -> new ResourceNotFoundException("Hotspot not found"));
        if (!h.getHost().getUser().getId().equals(hostId)) {
            throw new ForbiddenException("Not your hotspot");
        }
        h.setName(name);
        h.setDescription(description);
        h.setSsid(ssid);
        h.setApproximateArea(approximateArea);
        return toDto(hotspotRepository.save(h));
    }

    @Transactional
    public HotspotDto setAvailability(Long hotspotId, Long hostId, Boolean available) {
        Hotspot h = hotspotRepository.findById(hotspotId)
            .orElseThrow(() -> new ResourceNotFoundException("Hotspot not found"));
        if (!h.getHost().getUser().getId().equals(hostId)) {
            throw new ForbiddenException("Not your hotspot");
        }
        h.setAvailable(available);
        return toDto(hotspotRepository.save(h));
    }

    @Transactional(readOnly = true)
    public PagedHotspotResponse listByHost(Long hostId, Pageable pageable) {
        Page<Hotspot> page = hotspotRepository.findByHostUserId(hostId, pageable);
        return map(page);
    }

    private PagedHotspotResponse map(Page<Hotspot> page) {
        return new PagedHotspotResponse(
            page.getContent().stream().map(this::toDto).toList(),
            page.getNumber(), page.getSize(), page.getTotalElements(), page.getTotalPages()
        );
    }

    private HotspotDto toDto(Hotspot h) {
        return new HotspotDto(
            h.getId(), h.getName(), h.getDescription(),
            h.getLatitude(), h.getLongitude(), h.getSsid(), h.getApproximateArea(),
            h.getAvailable(), h.getStatus().name(),
            h.getHost().getUser().getId(), h.getHost().getBusinessName()
        );
    }
}


