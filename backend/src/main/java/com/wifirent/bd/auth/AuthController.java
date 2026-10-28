package com.wifirent.bd.auth;

import com.wifirent.bd.domain.audit.AuditAction;
import com.wifirent.bd.service.AdminService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import com.wifirent.bd.dto.AuthResponse;
import com.wifirent.bd.dto.LoginRequest;
import com.wifirent.bd.dto.MeResponse;
import com.wifirent.bd.dto.RegisterRequest;
import com.wifirent.bd.dto.TokenRefreshRequest;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;
    private final AdminService adminService;
    private final HttpServletRequest request;

    public AuthController(AuthService authService,
                          AdminService adminService,
                          HttpServletRequest request) {
        this.authService = authService;
        this.adminService = adminService;
        this.request = request;
    }

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@RequestBody RegisterRequest request) {
        AuthResponse response = authService.register(
            request.name(), request.email(), request.password());
        adminService.log(new AdminService.Actor(response.id(), "CUSTOMER"),
            AuditAction.CREATE, "USER", response.id().toString(),
            "User registered: " + request.email(), getClientIp());
        return ResponseEntity.ok(response);
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody LoginRequest request) {
        AuthResponse response = authService.login(request.email(), request.password());
        adminService.log(new AdminService.Actor(response.id(), response.role()),
            AuditAction.LOGIN, "USER", response.id().toString(),
            "User logged in: " + request.email(), getClientIp());
        return ResponseEntity.ok(response);
    }

    @PostMapping("/refresh")
    public ResponseEntity<AuthResponse> refresh(@RequestBody TokenRefreshRequest request) {
        AuthResponse response = authService.refresh(request.refreshToken());
        return ResponseEntity.ok(response);
    }

    @PostMapping("/logout")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> logout(@AuthenticationPrincipal AppUserDetails userDetails) {
        authService.logout(userDetails.getUser());
        adminService.log(new AdminService.Actor(userDetails.getUser().getId(),
            userDetails.getUser().getRole().name()),
            AuditAction.LOGOUT, "USER", userDetails.getUser().getId().toString(),
            "User logged out", getClientIp());
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/me")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<MeResponse> me(@AuthenticationPrincipal AppUserDetails userDetails) {
        return ResponseEntity.ok(authService.me(userDetails.getUser()));
    }

    private String getClientIp() {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("X-Real-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        return ip;
    }
}
