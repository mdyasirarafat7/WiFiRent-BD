package com.wifirent.bd.auth;

import com.wifirent.bd.domain.user.User;
import com.wifirent.bd.domain.user.UserRole;
import com.wifirent.bd.domain.user.repository.UserRepository;
import com.wifirent.bd.dto.AuthResponse;
import com.wifirent.bd.dto.MeResponse;
import com.wifirent.bd.exception.BadRequestException;
import com.wifirent.bd.exception.ConflictException;
import com.wifirent.bd.exception.ForbiddenException;
import com.wifirent.bd.service.WalletService;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.wifirent.bd.dto.*;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final WalletService walletService;

    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder,
                       JwtService jwtService, AuthenticationManager authenticationManager,
                       WalletService walletService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.authenticationManager = authenticationManager;
        this.walletService = walletService;
    }

    @Transactional
    public AuthResponse register(String name, String email, String password) {
        String normalizedEmail = email.trim().toLowerCase();
        if (userRepository.existsByEmail(normalizedEmail)) {
            throw new ConflictException("Email already in use");
        }

        User user = new User(name.trim(), normalizedEmail, passwordEncoder.encode(password), UserRole.CUSTOMER);
        User saved = userRepository.save(user);

        // Auto-create wallet with initial demo balance
        walletService.ensureWallet(saved.getId());

        String accessToken = jwtService.generateAccessToken(saved);
        String refreshToken = jwtService.generateRefreshToken(saved);

        return new AuthResponse(saved.getId(), saved.getName(), saved.getEmail(),
            saved.getRole().name(), accessToken, refreshToken);
    }

    public AuthResponse login(String email, String password) {
        User user = userRepository.findByEmail(email.trim().toLowerCase())
            .orElseThrow(() -> new BadRequestException("Invalid credentials"));

        if (!passwordEncoder.matches(password, user.getPasswordHash())) {
            throw new BadRequestException("Invalid credentials");
        }
        if (!user.getEnabled()) {
            throw new ForbiddenException("Account is disabled");
        }

        String accessToken = jwtService.generateAccessToken(user);
        String refreshToken = jwtService.generateRefreshToken(user);

        return new AuthResponse(user.getId(), user.getName(), user.getEmail(),
            user.getRole().name(), accessToken, refreshToken);
    }

    public AuthResponse refresh(String refreshToken) {
        String email = jwtService.extractUsername(refreshToken);
        User user = userRepository.findByEmail(email)
            .orElseThrow(() -> new BadRequestException("Invalid refresh token"));

        String accessToken = jwtService.generateAccessToken(user);
        String newRefreshToken = jwtService.generateRefreshToken(user);

        return new AuthResponse(user.getId(), user.getName(), user.getEmail(),
            user.getRole().name(), accessToken, newRefreshToken);
    }

    public MeResponse me(User user) {
        return new MeResponse(user.getId(), user.getName(), user.getEmail(),
            user.getRole().name(), user.getEnabled(), user.getEmailVerified());
    }

    @Transactional
    public void logout(User user) {
        user.setTokenVersion((user.getTokenVersion() != null ? user.getTokenVersion() : 0L) + 1);
        userRepository.save(user);
    }
}

