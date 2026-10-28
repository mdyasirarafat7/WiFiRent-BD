package com.wifirent.bd.controller;

import com.wifirent.bd.auth.AppUserDetails;
import com.wifirent.bd.domain.audit.AuditAction;
import com.wifirent.bd.service.AdminService;
import com.wifirent.bd.service.WalletService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import com.wifirent.bd.dto.PurchaseWithWalletRequest;
import com.wifirent.bd.dto.PurchaseWithWalletResponse;
import com.wifirent.bd.dto.RefundRequest;
import com.wifirent.bd.dto.RefundResponse;
import com.wifirent.bd.dto.WalletBalanceDto;
import com.wifirent.bd.dto.WalletTopUpRequest;
import com.wifirent.bd.dto.WalletTransferRequest;
import com.wifirent.bd.dto.WalletTransactionListDto;

@RestController
@RequestMapping("/api/wallet")
public class WalletController {

    private final WalletService walletService;
    private final AdminService adminService;

    public WalletController(WalletService walletService, AdminService adminService) {
        this.walletService = walletService;
        this.adminService = adminService;
    }

    @GetMapping("/balance")
    @PreAuthorize("hasAnyRole('CUSTOMER', 'HOST', 'ADMIN')")
    public ResponseEntity<WalletBalanceDto> balance(@AuthenticationPrincipal AppUserDetails userDetails) {
        WalletBalanceDto dto = walletService.getBalance(userDetails.getUser().getId());
        return ResponseEntity.ok(dto);
    }

    @PostMapping("/topup")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<WalletBalanceDto> topUp(@AuthenticationPrincipal AppUserDetails userDetails,
                                                       @Valid @RequestBody WalletTopUpRequest request) {
        WalletBalanceDto dto = walletService.topUp(userDetails.getUser().getId(), request);
        adminService.log(new AdminService.Actor(userDetails.getUser().getId(), "ADMIN"),
            AuditAction.WALLET_TOPUP, "WALLET", request.referenceId(),
            "Demo top-up: " + request.amountMinorUnits() + " credits to user " + request.targetUserId(),
            null);
        return ResponseEntity.ok(dto);
    }

    @PostMapping("/transfer")
    @PreAuthorize("hasAnyRole('CUSTOMER', 'HOST', 'ADMIN')")
    public ResponseEntity<WalletBalanceDto> transfer(@AuthenticationPrincipal AppUserDetails userDetails,
                                                          @Valid @RequestBody WalletTransferRequest request) {
        WalletBalanceDto dto = walletService.transfer(userDetails.getUser().getId(), request);
        adminService.log(new AdminService.Actor(userDetails.getUser().getId(),
            userDetails.getUser().getRole().name()),
            AuditAction.WALLET_TRANSFER, "WALLET_TRANSFER", request.referenceId(),
            "Demo transfer: " + request.amountMinorUnits() + " credits to user " + request.targetUserId(),
            null);
        return ResponseEntity.ok(dto);
    }

    @GetMapping("/transactions")
    @PreAuthorize("hasAnyRole('CUSTOMER', 'HOST', 'ADMIN')")
    public ResponseEntity<WalletTransactionListDto> transactions(@AuthenticationPrincipal AppUserDetails userDetails,
                                                                 @PageableDefault(sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        WalletTransactionListDto dto = walletService.listTransactions(userDetails.getUser().getId(), pageable);
        return ResponseEntity.ok(dto);
    }

    @PostMapping("/purchase")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<PurchaseWithWalletResponse> purchase(@AuthenticationPrincipal AppUserDetails userDetails,
                                                                     @Valid @RequestBody PurchaseWithWalletRequest request) {
        PurchaseWithWalletResponse dto = walletService.purchase(userDetails.getUser().getId(), request);
        adminService.log(new AdminService.Actor(userDetails.getUser().getId(), "CUSTOMER"),
            AuditAction.WALLET_PURCHASE, "RENTAL_ORDER", dto.orderId().toString(),
            "Demo wallet purchase: " + dto.customerDebitedMinorUnits() + " credits",
            null);
        return ResponseEntity.ok(dto);
    }

    @PostMapping("/refund")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<RefundResponse> refund(@AuthenticationPrincipal AppUserDetails userDetails,
                                                       @Valid @RequestBody RefundRequest request) {
        RefundResponse dto = walletService.refund(userDetails.getUser().getId(), request);
        adminService.log(new AdminService.Actor(userDetails.getUser().getId(), "ADMIN"),
            AuditAction.REFUND, "RENTAL_ORDER", request.rentalOrderId().toString(),
            "Demo refund: " + dto.amountMinorUnits() + " credits - " + request.reason(),
            null);
        return ResponseEntity.ok(dto);
    }
}
