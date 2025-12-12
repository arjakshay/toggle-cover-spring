package com.togglecover.user.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.togglecover.user.entity.UserProfile;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserProfileResponse {

    private String userId;
    private String fullName;
    private String email;
    private String city;
    private Integer age;
    private String emergencyContact;
    private String healthDeclaration;
    private String profilePictureUrl;

    private UserProfile.VerificationStatus verificationStatus;
    private UserProfile.KYCStatus kycStatus;
    private String verificationNotes; // Add this field

    private Map<String, Object> notificationPreferences;
    private Map<String, Object> communicationPreferences;
    private Map<String, Object> privacySettings;

    private String verifiedBy;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime verifiedAt;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime lastLogin;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updatedAt;

    // Additional fields
    private String occupation;
    private String companyName;
    private Double monthlyIncome;
    private String maritalStatus;
    private Integer dependents;
    private Boolean isActive;
    private Double profileCompletion;

    // Add these methods to the builder
    public static class UserProfileResponseBuilder {
        // Builder will automatically include all fields
    }
}