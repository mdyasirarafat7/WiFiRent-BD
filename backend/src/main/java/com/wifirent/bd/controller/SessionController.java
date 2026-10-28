package com.wifirent.bd.controller;

import com.wifirent.bd.auth.AppUserDetails;

import com.wifirent.bd.service.SessionService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import com.wifirent.bd.dto.*;

@RestController
@RequestMapping("/api/sessions")
public class SessionController {

    private final SessionService sessionService;

    public SessionController(SessionService sessionService) {
        this.sessionService = sessionService;
    }

    @GetMapping("/active")
    @PreAuthorize("hasAnyRole('CUSTOMER', 'HOST', 'ADMIN')")
    public ResponseEntity<ActiveSessionResponse> active(@AuthenticationPrincipal AppUserDetails userDetails) {
        return ResponseEntity.ok(sessionService.getActiveSession(userDetails.getUser().getId()));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('CUSTOMER', 'HOST', 'ADMIN')")
    public ResponseEntity<SessionDto> get(@PathVariable Long id,
                                                     @AuthenticationPrincipal AppUserDetails userDetails) {
        return ResponseEntity.ok(sessionService.getSession(id, userDetails.getUser()));
    }

    @GetMapping("/history")
    @PreAuthorize("hasAnyRole('CUSTOMER', 'HOST', 'ADMIN')")
    public ResponseEntity<?> history(@AuthenticationPrincipal AppUserDetails userDetails,
                                     org.springframework.data.domain.Pageable pageable) {
        return ResponseEntity.ok(sessionService.listHistory(userDetails.getUser(), pageable));
    }
}



