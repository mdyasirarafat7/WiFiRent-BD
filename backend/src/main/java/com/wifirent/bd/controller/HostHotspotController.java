package com.wifirent.bd.controller;

import com.wifirent.bd.auth.AppUserDetails;
import com.wifirent.bd.domain.audit.AuditAction;
import com.wifirent.bd.dto.AvailabilityRequest;
import com.wifirent.bd.dto.CreateHotspotRequest;
import com.wifirent.bd.dto.HotspotDto;
import com.wifirent.bd.dto.PagedHotspotResponse;
import com.wifirent.bd.dto.UpdateHotspotRequest;
import com.wifirent.bd.service.AdminService;
import com.wifirent.bd.service.HotspotService;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/host/hotspots")
@PreAuthorize("hasAnyRole('HOST', 'ADMIN')")
public class HostHotspotController {

    private final HotspotService hotspotService;
    private final AdminService adminService;

    public HostHotspotController(HotspotService hotspotService, AdminService adminService) {
        this.hotspotService = hotspotService;
        this.adminService = adminService;
    }

    @GetMapping
    public ResponseEntity<PagedHotspotResponse> listMine(@AuthenticationPrincipal AppUserDetails userDetails,
                                                         @PageableDefault(sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        PagedHotspotResponse response = hotspotService.listByHost(userDetails.getUser().getId(), pageable);
        return ResponseEntity.ok(response);
    }

    @PostMapping
    public ResponseEntity<HotspotDto> create(@AuthenticationPrincipal AppUserDetails userDetails,
                                             @RequestBody CreateHotspotRequest request) {
        HotspotDto dto = hotspotService.create(
            userDetails.getUser().getId(),
            request.name(), request.description(),
            request.latitude(), request.longitude(), request.ssid(), request.approximateArea()
        );
        adminService.log(new AdminService.Actor(userDetails.getUser().getId(),
            userDetails.getUser().getRole().name()),
            AuditAction.CREATE, "HOTSPOT", dto.id().toString(),
            "Host created hotspot: " + request.name(), null);
        return ResponseEntity.ok(dto);
    }

    @PutMapping("/{id}")
    public ResponseEntity<HotspotDto> update(@PathVariable Long id,
                                             @AuthenticationPrincipal AppUserDetails userDetails,
                                             @RequestBody UpdateHotspotRequest request) {
        HotspotDto dto = hotspotService.update(
            id, userDetails.getUser().getId(),
            request.name(), request.description(), request.ssid(), request.approximateArea()
        );
        adminService.log(new AdminService.Actor(userDetails.getUser().getId(),
            userDetails.getUser().getRole().name()),
            AuditAction.UPDATE, "HOTSPOT", id.toString(),
            "Host updated hotspot: " + id, null);
        return ResponseEntity.ok(dto);
    }

    @PatchMapping("/{id}/availability")
    public ResponseEntity<HotspotDto> setAvailability(@PathVariable Long id,
                                                      @AuthenticationPrincipal AppUserDetails userDetails,
                                                      @RequestBody AvailabilityRequest request) {
        HotspotDto dto = hotspotService.setAvailability(
            id, userDetails.getUser().getId(), request.available()
        );
        adminService.log(new AdminService.Actor(userDetails.getUser().getId(),
            userDetails.getUser().getRole().name()),
            request.available() ? AuditAction.UPDATE : AuditAction.UPDATE,
            "HOTSPOT", id.toString(),
            "Host set hotspot availability: " + id + " to " + request.available(), null);
        return ResponseEntity.ok(dto);
    }
}
