package com.togglecover.auth.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "user_sessions", indexes = {
        @Index(name = "idx_user_active", columnList = "userId,active"),
        @Index(name = "idx_login_time", columnList = "loginTime")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserSession {
    @Id
    private String id;

    @Column(nullable = false)
    private String userId;

    private String deviceId;
    private String deviceName;
    private String ipAddress;
    private String location;
    private String userAgent;

    private LocalDateTime loginTime;
    private LocalDateTime logoutTime;
    private LocalDateTime lastActivity;

    private String loginMethod;
    private String refreshToken;
    private Boolean active;

    @PrePersist
    protected void onCreate() {
        if (loginTime == null) {
            loginTime = LocalDateTime.now();
        }
        lastActivity = LocalDateTime.now();
        active = true;
    }

    @PreUpdate
    protected void onUpdate() {
        lastActivity = LocalDateTime.now();
    }
}