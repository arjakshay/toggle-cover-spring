package com.togglecover.user.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/debug")
@Slf4j
public class DebugController {

    @GetMapping("/auth")
    public Map<String, Object> checkAuth() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        log.info("Current authentication: {}", auth);
        log.info("Principal: {}", auth != null ? auth.getPrincipal() : null);
        log.info("Authorities: {}", auth != null ? auth.getAuthorities() : null);
        log.info("Is authenticated: {}", auth != null ? auth.isAuthenticated() : false);

        return Map.of(
                "authentication", auth != null ? auth.getName() : null,
                "principal", auth != null ? auth.getPrincipal().toString() : null,
                "authorities", auth != null ? auth.getAuthorities().toString() : null,
                "authenticated", auth != null ? auth.isAuthenticated() : false
        );
    }
}