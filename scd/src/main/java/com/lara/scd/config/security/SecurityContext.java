package com.lara.scd.config.security;

import com.lara.scd.user.domain.model.User;
import com.lara.scd.user.domain.repository.UserRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class SecurityContext {

    private final JwtUtil jwtUtil;
    private final UserRepository userRepository;

    public SecurityContext(JwtUtil jwtUtil, UserRepository userRepository) {
        this.jwtUtil = jwtUtil;
        this.userRepository = userRepository;
    }

    public String getCurrentUserEmail() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated() &&
                authentication.getPrincipal() instanceof UserDetails) {
            return ((UserDetails) authentication.getPrincipal()).getUsername();
        }
        return null;
    }

    public UUID getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated()) {
            Object credentials = authentication.getCredentials();
            if (credentials != null && credentials instanceof String) {
                String jwt = (String) credentials;
                try {
                    return jwtUtil.extractUserId(jwt);
                } catch (Exception e) {

                }
            }
        }
        return null;
    }

    public User getCurrentUser() {
        String email = getCurrentUserEmail();
        if (email == null) {
            throw new RuntimeException("Usuário não autenticado");
        }
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));
    }
}
