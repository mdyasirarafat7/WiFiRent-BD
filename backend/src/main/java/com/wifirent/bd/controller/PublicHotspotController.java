package com.wifirent.bd.controller;

import com.wifirent.bd.auth.AppUserDetails;
import com.wifirent.bd.domain.packageplan.repository.PackagePlanRepository;
import com.wifirent.bd.dto.NearbyHotspotRequest;
import com.wifirent.bd.dto.PagedHotspotResponse;
import com.wifirent.bd.service.HotspotService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import com.wifirent.bd.dto.*;

@RestController
@RequestMapping("/api/hotspots")
public class PublicHotspotController {

    private final HotspotService hotspotService;
    private final PackagePlanRepository packageRepository;

    public PublicHotspotController(HotspotService hotspotService,
                                   PackagePlanRepository packageRepository) {
        this.hotspotService = hotspotService;
        this.packageRepository = packageRepository;
    }

    @GetMapping
    public ResponseEntity<PagedHotspotResponse> list(@PageableDefault(sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        return ResponseEntity.ok(hotspotService.listAvailable(pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<HotspotDto> get(@PathVariable Long id) {
        return ResponseEntity.ok(hotspotService.getById(id));
    }

    @GetMapping("/nearby")
    public ResponseEntity<PagedHotspotResponse> nearby(@Valid NearbyHotspotRequest request) {
        return ResponseEntity.ok(hotspotService.findNearby(request.lat(), request.lng(), request.radiusMeters()));
    }

    @GetMapping("/{id}/packages")
    public ResponseEntity<?> getPackages(@PathVariable Long id) {
        return ResponseEntity.ok(packageRepository.findActiveByHotspotId(id, org.springframework.data.domain.PageRequest.of(0, 20)));
    }
}


