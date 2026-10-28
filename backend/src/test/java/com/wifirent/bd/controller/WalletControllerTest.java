package com.wifirent.bd.controller;

import com.wifirent.bd.auth.AppUserDetails;
import com.wifirent.bd.domain.audit.AuditAction;
import com.wifirent.bd.domain.user.User;
import com.wifirent.bd.domain.user.UserRole;
import com.wifirent.bd.domain.user.repository.UserRepository;
import com.wifirent.bd.service.AdminService;
import com.wifirent.bd.service.WalletService;
import com.wifirent.bd.dto.WalletBalanceDto;
import com.wifirent.bd.dto.WalletTopUpRequest;
import com.wifirent.bd.dto.WalletTransferRequest;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WalletControllerTest {

    @Mock
    private WalletService walletService;

    @Mock
    private AdminService adminService;

    @Test
    void balance_returnsWalletBalance() {
        User user = new User("Cust", "cust@example.com", "pass", UserRole.CUSTOMER);
        user.setId(1L);
        AppUserDetails details = new AppUserDetails(user);

        WalletController controller = new WalletController(walletService, adminService);
        when(walletService.getBalance(1L)).thenReturn(
            new WalletBalanceDto(1L, 5000L, "DEMO_CREDITS", "Demo credits - no real monetary value, not withdrawable"));

        ResponseEntity<WalletBalanceDto> response = controller.balance(details);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(5000L, response.getBody().balanceMinorUnits());
        assertTrue(response.getBody().note().contains("not withdrawable"));
    }

    @Test
    void topUp_requiresAdmin() {
        User nonAdmin = new User("Cust", "cust@example.com", "pass", UserRole.CUSTOMER);
        nonAdmin.setId(2L);
        AppUserDetails details = new AppUserDetails(nonAdmin);

        WalletController controller = new WalletController(walletService, adminService);
        WalletTopUpRequest req = new WalletTopUpRequest(2L, 1000L, "ref-1", "idem-1");

        when(walletService.topUp(2L, req))
            .thenThrow(new org.springframework.security.access.AccessDeniedException("Only admins can top up wallets"));

        assertThrows(org.springframework.security.access.AccessDeniedException.class, () ->
            controller.topUp(details, req));
    }

    @Test
    void transfer_cannotTransferToSelf() {
        User user = new User("Cust", "cust@example.com", "pass", UserRole.CUSTOMER);
        user.setId(1L);
        AppUserDetails details = new AppUserDetails(user);

        WalletController controller = new WalletController(walletService, adminService);
        WalletTransferRequest req = new WalletTransferRequest(1L, 100L, "self-1", "idem-self");

        when(walletService.transfer(1L, req))
            .thenThrow(new IllegalArgumentException("Cannot transfer to yourself"));

        assertThrows(IllegalArgumentException.class, () ->
            controller.transfer(details, req));
    }
}
