package com.togglecover.user.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final WebClient.Builder webClientBuilder;

    @Value("${services.auth.url}")
    private String authServiceUrl;

    public boolean validateToken(String token) {
        try {
            Map<String, Object> response = webClientBuilder.build()
                    .post()
                    .uri(authServiceUrl + "/api/v1/auth/token/validate")
                    .bodyValue(Map.of("token", token))
                    .retrieve()
                    .bodyToMono(Map.class)
                    .block();

            return Boolean.TRUE.equals(response.get("valid"));
        } catch (Exception e) {
            return false;
        }
    }
}