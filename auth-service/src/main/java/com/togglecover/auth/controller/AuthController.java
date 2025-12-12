package com.togglecover.auth.controller;

import com.togglecover.auth.dto.*;
import com.togglecover.auth.entity.OtpType;
import com.togglecover.auth.service.AuthService;
import com.togglecover.auth.service.OtpService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "Authentication endpoints")
public class AuthController {

    private final AuthService authService;
    private final OtpService otpService;

    @PostMapping("/register")
    @Operation(summary = "Register a new user")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        return ResponseEntity.ok(authService.register(request));
    }

    @PostMapping("/login")
    @Operation(summary = "Login user")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody AuthRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }

    @PostMapping("/otp/send")
    @Operation(summary = "Send OTP to phone number")
    public ResponseEntity<OtpResponse> sendOtp(@Valid @RequestBody OtpRequest request) {
        return ResponseEntity.ok(otpService.sendOtp(request.getPhone(), OtpType.LOGIN));
    }

    @PostMapping("/otp/verify")
    @Operation(summary = "Verify OTP for login")
    public ResponseEntity<AuthResponse> verifyOtp(@Valid @RequestBody VerifyOtpRequest request) {
        return ResponseEntity.ok(otpService.verifyOtp(request));
    }

    @PostMapping("/otp/forgot-password")
    @Operation(summary = "Send OTP for password reset")
    public ResponseEntity<OtpResponse> sendPasswordResetOtp(@Valid @RequestBody OtpRequest request) {
        return ResponseEntity.ok(otpService.sendOtp(request.getPhone(), OtpType.PASSWORD_RESET));
    }

    @PostMapping("/password/reset")
    @Operation(summary = "Reset password with OTP")
    public ResponseEntity<ApiResponse> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        // First verify OTP
        otpService.verifyOtpForPasswordReset(request.getPhone(), request.getOtp());

        // Then reset password
        return ResponseEntity.ok(authService.resetPassword(request));
    }

    @PostMapping("/validate")
    @Operation(summary = "Validate JWT token")
    public ResponseEntity<Boolean> validateToken(@RequestParam String token) {
        return ResponseEntity.ok(authService.validateToken(token));
    }
}