package com.togglecover.auth.service;

import com.togglecover.auth.dto.AuthResponse;
import com.togglecover.auth.dto.OtpResponse;
import com.togglecover.auth.dto.VerifyOtpRequest;
import com.togglecover.auth.entity.Otp;
import com.togglecover.auth.entity.OtpType;
import com.togglecover.auth.entity.User;
import com.togglecover.auth.repository.OtpRepository;
import com.togglecover.auth.repository.UserRepository;
import com.togglecover.auth.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Random;

@Service
@RequiredArgsConstructor
@Slf4j
public class OtpService {

    private final UserRepository userRepository;
    private final OtpRepository otpRepository;
    private final JwtUtil jwtUtil;
    private final NotificationService notificationService;

    @Value("${otp.length:6}")
    private Integer otpLength;

    @Value("${otp.expiration.minutes:5}")
    private Integer otpExpirationMinutes;

    @Value("${otp.max.retries:3}")
    private Integer maxRetries;

    @Transactional
    public OtpResponse sendOtp(String phone, OtpType otpType) {
        // Generate OTP
        String otpCode = generateOtp();

        // Save OTP to database
        Otp otp = Otp.builder()
                .phone(phone)
                .otp(otpCode)
                .type(otpType)
                .expiresAt(LocalDateTime.now().plusMinutes(otpExpirationMinutes))
                .retryCount(0)
                .isUsed(false)
                .build();

        otp = otpRepository.save(otp);

        // Send OTP via SMS/WhatsApp
        boolean sent = notificationService.sendOtp(phone, otpCode, otpType);

        if (!sent) {
            throw new RuntimeException("Failed to send OTP");
        }

        return OtpResponse.builder()
                .message("OTP sent successfully")
                .otpId(otp.getId())
                .maskedPhone(maskPhoneNumber(phone))
                .build();
    }

    @Transactional
    public AuthResponse verifyOtp(VerifyOtpRequest request) {
        // Find valid OTP
        Otp otp = otpRepository.findValidOtp(
                        request.getPhone(),
                        request.getOtp(),
                        OtpType.LOGIN,
                        LocalDateTime.now()
                )
                .orElseThrow(() -> new RuntimeException("Invalid or expired OTP"));

        // Check retry count
        if (otp.getRetryCount() >= maxRetries) {
            throw new RuntimeException("Maximum retry attempts exceeded");
        }

        // Mark OTP as used
        otp.setUsed(true);
        otp.setVerifiedAt(LocalDateTime.now());
        otpRepository.save(otp);

        // Find user and generate token
        User user = userRepository.findByPhone(request.getPhone())
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (user.getStatus() != User.UserStatus.ACTIVE) {
            throw new RuntimeException("Account is not active");
        }

        String token = jwtUtil.generateToken(user);

        return AuthResponse.builder()
                .token(token)
                .userId(user.getId())
                .phone(user.getPhone())
                .fullName(user.getFullName())
                .userType(user.getUserType())
                .expiresIn(86400000L) // 24 hours
                .build();
    }

    @Transactional
    public void verifyOtpForPasswordReset(String phone, String otpCode) {
        Otp otp = otpRepository.findValidOtp(
                        phone,
                        otpCode,
                        OtpType.PASSWORD_RESET,
                        LocalDateTime.now()
                )
                .orElseThrow(() -> new RuntimeException("Invalid or expired OTP"));

        otp.setUsed(true);
        otp.setVerifiedAt(LocalDateTime.now());
        otpRepository.save(otp);
    }

    private String generateOtp() {
        Random random = new Random();
        StringBuilder otp = new StringBuilder();

        for (int i = 0; i < otpLength; i++) {
            otp.append(random.nextInt(10));
        }

        return otp.toString();
    }

    private String maskPhoneNumber(String phone) {
        if (phone == null || phone.length() < 4) {
            return phone;
        }
        return "XXXXXX" + phone.substring(phone.length() - 4);
    }
}