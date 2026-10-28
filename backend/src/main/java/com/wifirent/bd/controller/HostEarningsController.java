package com.wifirent.bd.controller;

import com.wifirent.bd.auth.AppUserDetails;

import com.wifirent.bd.service.HostEarningsService;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import com.wifirent.bd.dto.*;

@RestController
@RequestMapping("/api/host")
@PreAuthorize("hasAnyRole('HOST', 'ADMIN')")
public class HostEarningsController {

    private final HostEarningsService earningsService;

    public HostEarningsController(HostEarningsService earningsService) {
        this.earningsService = earningsService;
    }

    @GetMapping("/earnings")
    public ResponseEntity<HostEarningsDto> earnings(@AuthenticationPrincipal AppUserDetails userDetails) {
        return ResponseEntity.ok(earningsService.getEarnings(userDetails.getUser().getId()));
    }

    @GetMapping("/ledger")
    public ResponseEntity<?> ledger(@AuthenticationPrincipal AppUserDetails userDetails,
                                    @PageableDefault(sort = "createdAt", direction = org.springframework.data.domain.Sort.Direction.DESC) Pageable pageable) {
        return ResponseEntity.ok(earningsService.getLedger(userDetails.getUser().getId(), pageable));
    }

    @GetMapping("/payouts")
    public ResponseEntity<?> payouts(@AuthenticationPrincipal AppUserDetails userDetails,
                                     @PageableDefault Pageable pageable) {
        return ResponseEntity.ok(earningsService.getPayouts(userDetails.getUser().getId(), pageable));
    }
}


