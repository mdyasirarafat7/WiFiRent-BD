package com.wifirent.bd.controller;

import com.wifirent.bd.auth.AppUserDetails;

import com.wifirent.bd.service.RentalService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import com.wifirent.bd.dto.*;

@RestController
@RequestMapping("/api/rentals")
public class RentalController {

    private final RentalService rentalService;

    public RentalController(RentalService rentalService) {
        this.rentalService = rentalService;
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('CUSTOMER', 'HOST', 'ADMIN')")
    public ResponseEntity<RentalOrderDto> create(@AuthenticationPrincipal AppUserDetails userDetails,
                                                           @Valid @RequestBody CreateRentalOrderRequest request) {
        return ResponseEntity.ok(rentalService.createOrder(userDetails.getUser().getId(), request));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('CUSTOMER', 'HOST', 'ADMIN')")
    public ResponseEntity<?> list(@AuthenticationPrincipal AppUserDetails userDetails,
                                  @PageableDefault(sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        return ResponseEntity.ok(rentalService.listOrders(userDetails.getUser(), pageable));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('CUSTOMER', 'HOST', 'ADMIN')")
    public ResponseEntity<RentalOrderDto> get(@PathVariable Long id,
                                                        @AuthenticationPrincipal AppUserDetails userDetails) {
        return ResponseEntity.ok(rentalService.getOrder(id, userDetails.getUser()));
    }

    @PostMapping("/{id}/cancel")
    @PreAuthorize("hasAnyRole('CUSTOMER', 'HOST', 'ADMIN')")
    public ResponseEntity<RentalOrderDto> cancel(@PathVariable Long id,
                                                           @AuthenticationPrincipal AppUserDetails userDetails,
                                                           @RequestBody(required = false) CancelRentalRequest request) {
        return ResponseEntity.ok(rentalService.cancelOrder(id, userDetails.getUser(),
            request != null ? request.reason() : null));
    }
}


