package com.wifirent.bd.controller;

import com.wifirent.bd.auth.AppUserDetails;

import com.wifirent.bd.dto.PagedHotspotResponse;
import com.wifirent.bd.service.AdminService;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import com.wifirent.bd.dto.*;

@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private final AdminService adminService;

    public AdminController(AdminService adminService) {
        this.adminService = adminService;
    }

    @GetMapping("/users")
    public ResponseEntity<?> listUsers(@PageableDefault Pageable pageable) {
        return ResponseEntity.ok(adminService.listUsers(pageable));
    }

    @GetMapping("/hotspots")
    public ResponseEntity<PagedHotspotResponse> listHotspots(@PageableDefault(sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        return ResponseEntity.ok(adminService.listHotspots(pageable));
    }

    @PatchMapping("/hotspots/{id}/status")
    public ResponseEntity<HotspotDto> setHotspotStatus(@PathVariable Long id,
                                                       @RequestParam String status) {
        try {
            return ResponseEntity.ok(adminService.setHotspotStatus(id, status));
        } catch (IllegalArgumentException e) {
            throw new com.wifirent.bd.exception.BadRequestException(e.getMessage());
        }
    }

    @GetMapping("/payments")
    public ResponseEntity<?> listPayments(@PageableDefault Pageable pageable) {
        return ResponseEntity.ok(adminService.listPayments(pageable));
    }

    @GetMapping("/sessions")
    public ResponseEntity<?> listSessions(@PageableDefault Pageable pageable) {
        return ResponseEntity.ok(adminService.listSessions(pageable));
    }

    @GetMapping("/audit-logs")
    public ResponseEntity<?> listAuditLogs(@PageableDefault(sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable,
                                           @AuthenticationPrincipal AppUserDetails userDetails) {
        return ResponseEntity.ok(adminService.listAuditLogs(pageable, userDetails.getUser().getId()));
    }
}


