package com.lara.scd.config.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class SecurityContext {

    private final JwtUtil jwtUtil;

    public SecurityContext(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
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
}