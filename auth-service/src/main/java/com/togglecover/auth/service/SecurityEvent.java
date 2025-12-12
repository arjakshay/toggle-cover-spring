package com.togglecover.auth.service;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SecurityEvent {
    private String userId;
    private String eventType;
    private String description;
    private String ipAddress;
    private String userAgent;
    private LocalDateTime timestamp;
    private String severity; // LOW, MEDIUM, HIGH, CRITICAL
}