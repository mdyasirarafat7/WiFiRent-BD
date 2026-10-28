package com.wifirent.bd.auth;

import com.wifirent.bd.domain.user.User;
import com.wifirent.bd.domain.user.UserRole;
import com.wifirent.bd.domain.user.repository.UserRepository;
import com.wifirent.bd.domain.wallet.Wallet;
import com.wifirent.bd.exception.ConflictException;
import com.wifirent.bd.service.WalletService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.ArgumentCaptor;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceRegisterTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    @Mock
    private org.springframework.security.authentication.AuthenticationManager authenticationManager;

    @Mock
    private WalletService walletService;

    @InjectMocks
    private AuthService authService;

    @Test
    void register_shouldAlwaysCreateCustomerRole() {
        when(userRepository.existsByEmail("test@example.com")).thenReturn(false);
        when(passwordEncoder.encode("password123")).thenReturn("hashed");
        when(userRepository.save(any(User.class))).thenAnswer(inv -> {
            User u = inv.getArgument(0);
            u.setId(1L);
            return u;
        });
        when(jwtService.generateAccessToken(any(User.class))).thenReturn("access-token");
        when(jwtService.generateRefreshToken(any(User.class))).thenReturn("refresh-token");
        when(walletService.ensureWallet(any(Long.class))).thenAnswer(inv -> {
            Wallet w = new Wallet();
            w.setId(100L);
            return w;
        });

        var response = authService.register("Test User", "test@example.com", "password123");

        assertEquals("CUSTOMER", response.role());
        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(captor.capture());
        assertEquals(UserRole.CUSTOMER, captor.getValue().getRole());
    }

    @Test
    void register_shouldNotAllowRoleSelection() {
        when(userRepository.existsByEmail("test@example.com")).thenReturn(false);
        when(passwordEncoder.encode("password123")).thenReturn("hashed");
        when(userRepository.save(any(User.class))).thenAnswer(inv -> {
            User u = inv.getArgument(0);
            u.setId(1L);
            return u;
        });
        when(jwtService.generateAccessToken(any(User.class))).thenReturn("access-token");
        when(jwtService.generateRefreshToken(any(User.class))).thenReturn("refresh-token");
        when(walletService.ensureWallet(any(Long.class))).thenAnswer(inv -> {
            Wallet w = new Wallet();
            w.setId(100L);
            return w;
        });

        var response = authService.register("Test User", "test@example.com", "password123");

        // Registration always creates CUSTOMER, never ADMIN or HOST
        assertEquals("CUSTOMER", response.role());
        assertNotEquals("ADMIN", response.role());
        assertNotEquals("HOST", response.role());
    }
}
