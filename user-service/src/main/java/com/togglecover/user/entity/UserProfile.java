package com.togglecover.user.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.Map;

@Entity
@Table(name = "user_profiles", indexes = {
        @Index(name = "idx_user_id", columnList = "userId"),
        @Index(name = "idx_email", columnList = "email"),
        @Index(name = "idx_city", columnList = "city"),
        @Index(name = "idx_verification_status", columnList = "verificationStatus"),
        @Index(name = "idx_kyc_status", columnList = "kycStatus")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserProfile {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(unique = true, nullable = false)
    private String userId;

    private String fullName;
    private String email;
    private String city;
    private Integer age;
    private String emergencyContact;

    @Column(length = 2000)
    private String healthDeclaration;

    private String profilePictureUrl;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private VerificationStatus verificationStatus = VerificationStatus.PENDING;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private KYCStatus kycStatus = KYCStatus.PENDING;

    private String verificationNotes;
    private String verifiedBy;
    private LocalDateTime verifiedAt;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    private Map<String, Object> notificationPreferences;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    private Map<String, Object> communicationPreferences;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    private Map<String, Object> privacySettings;

    private LocalDateTime lastLogin;
    private LocalDateTime lastActivity;

    @Builder.Default
    private Boolean deleted = false;
    private LocalDateTime deletedAt;

    private String lastUpdatedBy;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Additional fields for gig workers
    private String occupation;
    private String companyName;
    private Double monthlyIncome;
    private String maritalStatus;
    private Integer dependents;

    @Builder.Default
    private Boolean isActive = true;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (verificationStatus == null) {
            verificationStatus = VerificationStatus.PENDING;
        }
        if (kycStatus == null) {
            kycStatus = KYCStatus.PENDING;
        }
        if (deleted == null) {
            deleted = false;
        }
        if (isActive == null) {
            isActive = true;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public enum VerificationStatus {
        PENDING,
        VERIFIED,
        REJECTED,
        UNDER_REVIEW,
        SUSPENDED
    }

    public enum KYCStatus {
        PENDING,
        IN_PROGRESS,
        COMPLETED,
        FAILED,
        EXPIRED
    }
}