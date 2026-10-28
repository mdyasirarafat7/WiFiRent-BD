package com.wifirent.bd.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.AuditorAware;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import java.util.Optional;

@Configuration
public class JpaAuditConfig {

    @Bean
    public AuditorAware<Long> auditorAware() {
        return () -> {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth != null && auth.isAuthenticated() && auth.getPrincipal() instanceof com.wifirent.bd.auth.AppUserDetails) {
                return Optional.of(((com.wifirent.bd.auth.AppUserDetails) auth.getPrincipal()).getUser().getId());
            }
            return Optional.empty();
        };
    }
}
