package com.togglecover.user.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthClientService {

    private final WebClient.Builder webClientBuilder;

    @Value("${services.auth.url}")
    private String authServiceUrl;

    public boolean validateToken(String token) {
        try {
            Map response = webClientBuilder.build()
                    .post()
                    .uri(authServiceUrl + "/api/v1/auth/token/validate")
                    .bodyValue(Map.of("token", token))
                    .retrieve()
                    .bodyToMono(Map.class)
                    .block();

            return Boolean.TRUE.equals(response != null ? response.get("valid") : false);
        } catch (Exception e) {
            log.error("Failed to validate token: {}", e.getMessage());
            return false;
        }
    }

    public Map getUserInfo(String userId) {
        try {
            return webClientBuilder.build()
                    .get()
                    .uri(authServiceUrl + "/api/v1/auth/token/user-info")
                    .retrieve()
                    .bodyToMono(Map.class)
                    .block();
        } catch (Exception e) {
            log.error("Failed to get user info for userId: {}", userId, e);
            return Map.of();
        }
    }
}