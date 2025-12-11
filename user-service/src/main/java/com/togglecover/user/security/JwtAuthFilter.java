package com.togglecover.user.security;

import com.togglecover.user.feign.AuthServiceClient;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class JwtAuthFilter extends OncePerRequestFilter {

    private final AuthServiceClient authServiceClient;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {

        final String authorizationHeader = request.getHeader("Authorization");
        log.debug("JwtAuthFilter - Request URI: {}", request.getRequestURI());
        log.debug("JwtAuthFilter - Authorization Header: {}", authorizationHeader);

        String token = null;
        String userId = null;
        String userType = null;

        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            token = authorizationHeader.substring(7);
            log.debug("JwtAuthFilter - Extracted token: {}", token.substring(0, Math.min(20, token.length())) + "...");

            try {
                // Create request body with token
                Map<String, String> requestBody = new HashMap<>();
                requestBody.put("token", token);

                log.debug("JwtAuthFilter - Calling auth-service with token");
                ResponseEntity<Map<String, Object>> validationResponse =
                        authServiceClient.validateToken(requestBody);

                log.debug("JwtAuthFilter - Auth service response status: {}", validationResponse.getStatusCode());

                Map<String, Object> responseBody = validationResponse.getBody();
                log.debug("JwtAuthFilter - Auth service response body: {}", responseBody);

                if (responseBody != null) {
                    Boolean isValid = (Boolean) responseBody.get("valid");
                    log.debug("JwtAuthFilter - Token validation result: {}", isValid);

                    if (Boolean.TRUE.equals(isValid)) {
                        userId = (String) responseBody.get("userId");
                        userType = (String) responseBody.get("userType");
                        log.debug("JwtAuthFilter - Extracted userId: {}, userType: {}", userId, userType);
                    } else {
                        log.warn("JwtAuthFilter - Token is invalid");
                    }
                } else {
                    log.warn("JwtAuthFilter - Auth service returned null response body");
                }
            } catch (Exception e) {
                log.error("JwtAuthFilter - Error validating token: {}", e.getMessage(), e);
                if (e.getMessage() != null && e.getMessage().contains("403")) {
                    log.error("JwtAuthFilter - 403 Forbidden from auth-service");
                }
            }
        } else {
            log.debug("JwtAuthFilter - No valid Authorization header found");
        }

        if (userId != null) {
            log.debug("JwtAuthFilter - Setting authentication for userId: {}", userId);
            SimpleGrantedAuthority authority = new SimpleGrantedAuthority(userType != null ? userType : "USER");

            UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                    userId, null, Collections.singletonList(authority));

            SecurityContextHolder.getContext().setAuthentication(authentication);
            log.debug("JwtAuthFilter - Authentication set successfully");
        } else {
            log.debug("JwtAuthFilter - No userId extracted, authentication not set");
        }

        chain.doFilter(request, response);
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getServletPath();
        log.debug("JwtAuthFilter - Checking shouldNotFilter for path: {}", path);

        boolean shouldNotFilter = path.startsWith("/actuator") ||
                path.startsWith("/swagger-ui") ||
                path.startsWith("/swagger-ui/") ||
                path.startsWith("/v3/api-docs") ||
                path.startsWith("/api-docs") ||
                path.startsWith("/webjars/") ||
                path.startsWith("/swagger-resources") ||
                path.equals("/swagger-ui.html") ||
                path.equals("/favicon.ico") ||
                path.startsWith("/h2-console");

        log.debug("JwtAuthFilter - shouldNotFilter for {}: {}", path, shouldNotFilter);
        return shouldNotFilter;
    }
}