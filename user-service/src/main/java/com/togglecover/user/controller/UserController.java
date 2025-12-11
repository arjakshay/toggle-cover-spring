package com.togglecover.user.controller;

import com.togglecover.user.dto.UserProfileRequest;
import com.togglecover.user.dto.UserProfileResponse;
import com.togglecover.user.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
@Tag(name = "User Management", description = "User profile management endpoints")
public class UserController {

    private final UserService userService;

    @GetMapping("/profile")
    @Operation(
            summary = "Get current user profile",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    public ResponseEntity<UserProfileResponse> getCurrentUserProfile() {
        String userId = getCurrentUserId();
        return ResponseEntity.ok(userService.getUserProfile(userId));
    }

    @GetMapping("/{userId}")
    @Operation(
            summary = "Get user profile by ID",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    public ResponseEntity<UserProfileResponse> getUserProfile(@PathVariable String userId) {
        // Add authorization check here if needed
        return ResponseEntity.ok(userService.getUserProfile(userId));
    }

    @PutMapping("/profile")
    @Operation(
            summary = "Update current user profile",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    public ResponseEntity<UserProfileResponse> updateProfile(
            @Valid @RequestBody UserProfileRequest request) {
        String userId = getCurrentUserId();
        return ResponseEntity.ok(userService.updateProfile(userId, request));
    }

    @GetMapping("/exists/{userId}")
    @Operation(
            summary = "Check if user exists",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    public ResponseEntity<Boolean> userExists(@PathVariable String userId) {
        return ResponseEntity.ok(userService.userExists(userId));
    }

    private String getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication.getName();
    }
}