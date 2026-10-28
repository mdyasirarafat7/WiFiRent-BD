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
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

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
    void register_shouldCreateUserAndReturnAuthResponse() {
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

        assertEquals(1L, response.id());
        assertEquals("test@example.com", response.email());
        assertEquals("access-token", response.accessToken());
        verify(userRepository).save(any(User.class));
    }

    @Test
    void register_shouldThrowWhenEmailExists() {
        when(userRepository.existsByEmail("test@example.com")).thenReturn(true);

        assertThrows(ConflictException.class, () ->
            authService.register("Test User", "test@example.com", "password123"));
    }

    @Test
    void login_shouldReturnAuthResponseWhenCredentialsValid() {
        User user = new User("Test", "test@example.com", "hashed", UserRole.CUSTOMER);
        user.setId(1L);
        when(userRepository.findByEmail("test@example.com")).thenReturn(java.util.Optional.of(user));
        when(passwordEncoder.matches("password123", "hashed")).thenReturn(true);
        when(jwtService.generateAccessToken(user)).thenReturn("access-token");
        when(jwtService.generateRefreshToken(user)).thenReturn("refresh-token");

        var response = authService.login("test@example.com", "password123");

        assertEquals("test@example.com", response.email());
        assertEquals("access-token", response.accessToken());
    }

    @Test
    void login_shouldThrowWhenPasswordInvalid() {
        User user = new User("Test", "test@example.com", "hashed", UserRole.CUSTOMER);
        when(userRepository.findByEmail("test@example.com")).thenReturn(java.util.Optional.of(user));
        when(passwordEncoder.matches("wrong", "hashed")).thenReturn(false);

        assertThrows(com.wifirent.bd.exception.BadRequestException.class, () ->
            authService.login("test@example.com", "wrong"));
    }

    @Test
    void login_shouldThrowWhenUserNotFound() {
        when(userRepository.findByEmail("none@example.com")).thenReturn(java.util.Optional.empty());

        assertThrows(com.wifirent.bd.exception.BadRequestException.class, () ->
            authService.login("none@example.com", "password"));
    }
}
