package com.togglecover.auth.service;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class DeviceFingerprintService {

    private final HttpServletRequest request;

    public String getDeviceFingerprint() {
        try {
            // Collect various device attributes
            String userAgent = request.getHeader("User-Agent") != null ?
                    request.getHeader("User-Agent") : "";

            String acceptLanguage = request.getHeader("Accept-Language") != null ?
                    request.getHeader("Accept-Language") : "";

            String acceptEncoding = request.getHeader("Accept-Encoding") != null ?
                    request.getHeader("Accept-Encoding") : "";

            String screenResolution = request.getHeader("X-Screen-Resolution") != null ?
                    request.getHeader("X-Screen-Resolution") : "";

            String platform = request.getHeader("X-Platform") != null ?
                    request.getHeader("X-Platform") : "";

            String deviceId = request.getHeader("X-Device-Id");

            // If device ID is provided, use it
            if (deviceId != null && !deviceId.trim().isEmpty()) {
                return deviceId;
            }

            // Create fingerprint from available attributes
            String fingerprintData = String.join("|",
                    userAgent,
                    acceptLanguage,
                    acceptEncoding,
                    screenResolution,
                    platform,
                    getClientIp()
            );

            // Generate MD5 hash of the fingerprint data
            return DigestUtils.md5DigestAsHex(fingerprintData.getBytes(StandardCharsets.UTF_8));

        } catch (Exception e) {
            log.error("Error generating device fingerprint: {}", e.getMessage());
            // Return a random UUID as fallback
            return UUID.randomUUID().toString();
        }
    }

    public String getClientIp() {
        String xfHeader = request.getHeader("X-Forwarded-For");
        if (xfHeader != null) {
            return xfHeader.split(",")[0];
        }
        return request.getRemoteAddr();
    }
}