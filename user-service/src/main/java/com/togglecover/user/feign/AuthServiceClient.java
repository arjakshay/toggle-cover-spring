package com.togglecover.user.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.Map;

@FeignClient(name = "auth-service", url = "${services.auth.url}")
public interface AuthServiceClient {

    @PostMapping("/api/v1/auth/token/validate")
    ResponseEntity<Map<String, Object>> validateToken(@RequestBody Map<String, String> request);

    @PostMapping("/api/v1/auth/token/user-info")
    ResponseEntity<Map<String, Object>> getUserInfo(@RequestBody Map<String, String> request);
}