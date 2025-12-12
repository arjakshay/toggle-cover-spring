package com.togglecover.user.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class SecurityService {

    public boolean canAccessUser(Authentication authentication, String userId) {
        String currentUserId = authentication.getName();
        boolean isAdmin = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch(auth -> auth.equals("ROLE_ADMIN") || auth.equals("ADMIN"));

        return isAdmin || currentUserId.equals(userId);
    }
}