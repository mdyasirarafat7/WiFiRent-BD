package com.wifirent.bd.controller;

import com.wifirent.bd.auth.AppUserDetails;
import com.wifirent.bd.domain.audit.AuditAction;
import com.wifirent.bd.service.AdminService;
import com.wifirent.bd.service.WalletService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import com.wifirent.bd.dto.PurchaseWithWalletRequest;
import com.wifirent.bd.dto.PurchaseWithWalletResponse;

@RestController
@RequestMapping("/api/payments")
public class PaymentController {

    private final WalletService walletService;
    private final AdminService adminService;

    public PaymentController(WalletService walletService, AdminService adminService) {
        this.walletService = walletService;
        this.adminService = adminService;
    }

    /**
     * Deprecated: real-payment checkout is no longer supported.
     * Use POST /api/wallet/purchase instead for demo-credit purchases.
     */
    @PostMapping("/checkout")
    @PreAuthorize("hasAnyRole('CUSTOMER', 'HOST', 'ADMIN')")
    public ResponseEntity<Void> checkout(@AuthenticationPrincipal AppUserDetails userDetails,
                                                 @RequestBody(required = false) Object body) {
        return ResponseEntity.status(HttpStatus.GONE).build();
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('CUSTOMER', 'HOST', 'ADMIN')")
    public ResponseEntity<Void> get(@PathVariable Long id,
                                             @AuthenticationPrincipal AppUserDetails userDetails) {
        return ResponseEntity.status(HttpStatus.GONE).build();
    }
}
