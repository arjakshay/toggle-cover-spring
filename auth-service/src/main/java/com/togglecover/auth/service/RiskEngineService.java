package com.togglecover.auth.service;

import com.togglecover.auth.dto.RegisterRequest;
import com.togglecover.auth.dto.VerifyOtpRequest;
import com.togglecover.auth.entity.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@Slf4j
public class RiskEngineService {

    private final RedisTemplate<String, Object> redisTemplate;
    private final GeoLocationService geoLocationService;
    private final DeviceFingerprintService deviceFingerprintService;

    public RiskAssessment assessRegistrationRisk(RegisterRequest request) {
        int riskScore = 0;
        StringBuilder reasons = new StringBuilder();

        // 1. Check for disposable email
        if (isDisposableEmail(request.getEmail())) {
            riskScore += 20;
            reasons.append("Disposable email detected; ");
        }

        // 2. Check age
        if (request.getAge() < 18 || request.getAge() > 65) {
            riskScore += 15;
            reasons.append("Age outside typical range; ");
        }

        // 3. Check phone number reputation
        if (isSuspiciousPhoneNumber(request.getPhone())) {
            riskScore += 25;
            reasons.append("Suspicious phone number; ");
        }

        // 4. Check if multiple registrations from same IP/Device
        String registrationKey = "reg:ip:" + getClientIp();
        Integer regCount = (Integer) redisTemplate.opsForValue().get(registrationKey);

        if (regCount != null && regCount > 3) {
            riskScore += 30;
            reasons.append("Multiple registrations from same source; ");
        }

        redisTemplate.opsForValue().increment(registrationKey, 1);
        redisTemplate.expire(registrationKey, 24, TimeUnit.HOURS);

        boolean highRisk = riskScore > 50;

        return RiskAssessment.builder()
                .riskScore(riskScore)
                .highRisk(highRisk)
                .reasons(reasons.toString())
                .build();
    }

    public RiskAssessment assessLoginRisk(User user, Object loginRequest) {
        int riskScore = 0;
        StringBuilder reasons = new StringBuilder();

        // 1. Check failed login attempts
        String failedLoginKey = "failed:login:" + user.getPhone();
        Integer failedAttempts = (Integer) redisTemplate.opsForValue().get(failedLoginKey);

        if (failedAttempts != null && failedAttempts > 5) {
            riskScore += 25;
            reasons.append("Multiple failed login attempts; ");
        }

        // 2. Check location anomaly
        String lastLocationKey = "user:location:" + user.getId();
        String lastLocation = (String) redisTemplate.opsForValue().get(lastLocationKey);
        String currentLocation = geoLocationService.getLocation(getClientIp());

        if (lastLocation != null && !lastLocation.equals(currentLocation)) {
            // Check if distance is suspicious (e.g., login from different country within short time)
            riskScore += 20;
            reasons.append("Unusual location detected; ");
        }

        redisTemplate.opsForValue().set(lastLocationKey, currentLocation, 7, TimeUnit.DAYS);

        // 3. Check device fingerprint
        String deviceFingerprint = deviceFingerprintService.getDeviceFingerprint();
        String knownDevicesKey = "user:devices:" + user.getId();

        if (!redisTemplate.opsForSet().isMember(knownDevicesKey, deviceFingerprint)) {
            riskScore += 15;
            reasons.append("New device detected; ");
            // Add to known devices
            redisTemplate.opsForSet().add(knownDevicesKey, deviceFingerprint);
        }

        // 4. Check time anomaly (unusual login hours)
        int hour = LocalDateTime.now().getHour();
        if (hour < 6 || hour > 23) { // Login between 11 PM and 6 AM
            riskScore += 10;
            reasons.append("Unusual login time; ");
        }

        boolean highRisk = riskScore > 40;

        return RiskAssessment.builder()
                .riskScore(riskScore)
                .highRisk(highRisk)
                .reasons(reasons.toString())
                .requiresAdditionalAuth(riskScore > 30)
                .build();
    }

    public RiskAssessment assessOtpLoginRisk(User user, VerifyOtpRequest request) {
        RiskAssessment loginRisk = assessLoginRisk(user, request);

        // Additional OTP-specific checks
        int riskScore = loginRisk.getRiskScore();

        // Check OTP request frequency
        String otpRequestKey = "otp:requests:" + user.getPhone();
        Integer otpRequests = (Integer) redisTemplate.opsForValue().get(otpRequestKey);

        if (otpRequests != null && otpRequests > 10) {
            riskScore += 20;
            loginRisk.setReasons(loginRisk.getReasons() + "Excessive OTP requests; ");
        }

        redisTemplate.opsForValue().increment(otpRequestKey, 1);
        redisTemplate.expire(otpRequestKey, 1, TimeUnit.HOURS);

        loginRisk.setRiskScore(riskScore);
        loginRisk.setHighRisk(riskScore > 40);
        loginRisk.setRequiresAdditionalAuth(riskScore > 25);

        return loginRisk;
    }

    public void recordFailedLoginAttempt(String phone) {
        String key = "failed:login:" + phone;
        redisTemplate.opsForValue().increment(key, 1);
        redisTemplate.expire(key, 1, TimeUnit.HOURS);
    }

    public void logSecurityEvent(String userId, String eventType, String description) {
        SecurityEvent event = SecurityEvent.builder()
                .userId(userId)
                .eventType(eventType)
                .description(description)
                .ipAddress(getClientIp())
                .userAgent(getUserAgent())
                .timestamp(LocalDateTime.now())
                .build();

        // Store in database or send to SIEM
        log.warn("Security Event: {} - {} - {}", userId, eventType, description);
    }

    private boolean isDisposableEmail(String email) {
        // Implement check against known disposable email domains
        String[] disposableDomains = {"tempmail.com", "mailinator.com", "guerrillamail.com"};
        for (String domain : disposableDomains) {
            if (email.endsWith("@" + domain)) {
                return true;
            }
        }
        return false;
    }

    private boolean isSuspiciousPhoneNumber(String phone) {
        // Check against known fraudulent patterns
        // In production, integrate with phone validation service
        return false;
    }

    private String getClientIp() {
        // Get from request context
        return "127.0.0.1"; // Placeholder
    }

    private String getUserAgent() {
        // Get from request context
        return "Unknown"; // Placeholder
    }
}