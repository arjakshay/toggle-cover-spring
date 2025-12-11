package com.togglecover.auth.controller;

import com.togglecover.auth.security.JwtUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/auth/token")
@RequiredArgsConstructor
@Tag(name = "Token Management", description = "Token validation and information endpoints")
public class TokenController {

    private final JwtUtil jwtUtil;

    @PostMapping("/validate")
    @Operation(summary = "Validate JWT token")
    public ResponseEntity<Map<String, Object>> validateToken(@RequestBody Map<String, String> request) {
        String token = request.get("token");
        boolean isValid = jwtUtil.validateToken(token);

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("valid", isValid);

        try {
            response.put("userId", isValid ? jwtUtil.extractUserId(token) : null);
            response.put("userType", isValid ? jwtUtil.extractUserType(token).name() : null);
        } catch (Exception e) {
            response.put("userId", null);
            response.put("userType", null);
        }

        return ResponseEntity.ok(response);
    }

    @PostMapping("/user-info")
    @Operation(summary = "Get user information from token")
    public ResponseEntity<Map<String, Object>> getUserInfo(@RequestBody Map<String, String> request) {
        String token = request.get("token");

        Map<String, Object> userInfo = Map.of(
                "userId", jwtUtil.extractUserId(token),
                "phone", jwtUtil.extractUsername(token),
                "userType", jwtUtil.extractUserType(token).name()
        );

        return ResponseEntity.ok(userInfo);
    }

    @PostMapping("/debug")
    public Map<String, Object> debugToken(@RequestBody Map<String, String> request) {
        String token = request.get("token");
        return jwtUtil.debugToken(token);
    }
}