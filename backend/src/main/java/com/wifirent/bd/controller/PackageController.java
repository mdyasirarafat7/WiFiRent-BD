package com.wifirent.bd.controller;

import com.wifirent.bd.auth.AppUserDetails;

import com.wifirent.bd.service.PackageService;
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
@RequestMapping("/api/host/packages")
@PreAuthorize("hasAnyRole('HOST', 'ADMIN')")
public class PackageController {

    private final PackageService packageService;

    public PackageController(PackageService packageService) {
        this.packageService = packageService;
    }

    @PostMapping("/hotspots/{hotspotId}")
    public ResponseEntity<PackagePlanDto> create(@PathVariable Long hotspotId,
                                                            @AuthenticationPrincipal AppUserDetails userDetails,
                                                            @Valid @RequestBody CreatePackageRequest request) {
        return ResponseEntity.ok(packageService.create(hotspotId, userDetails.getUser().getId(), request));
    }

    @PutMapping("/{packageId}")
    public ResponseEntity<PackagePlanDto> update(@PathVariable Long packageId,
                                                            @AuthenticationPrincipal AppUserDetails userDetails,
                                                            @Valid @RequestBody UpdatePackageRequest request) {
        return ResponseEntity.ok(packageService.update(packageId, userDetails.getUser().getId(), request));
    }

    @DeleteMapping("/{packageId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable Long packageId) {
        packageService.deactivate(packageId);
        return ResponseEntity.noContent().build();
    }
}


