package com.togglecover.auth.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "otps", indexes = {
        @Index(name = "idx_phone_type", columnList = "phone,type"),
        @Index(name = "idx_expires_at", columnList = "expiresAt")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Otp {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(nullable = false)
    private String phone;

    @Column(nullable = false)
    private String otp;

    @Enumerated(EnumType.STRING)
    private OtpType type;

    private LocalDateTime expiresAt;

    @Builder.Default
    private Integer retryCount = 0;

    @Builder.Default
    private Boolean isUsed = false;

    private LocalDateTime verifiedAt;
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        if (retryCount == null) {
            retryCount = 0;
        }
        if (isUsed == null) {
            isUsed = false;
        }
    }

    // Custom setters for builder compatibility
    public void setUsed(Boolean used) {
        isUsed = used;
    }

    public Boolean getUsed() {
        return isUsed;
    }
}