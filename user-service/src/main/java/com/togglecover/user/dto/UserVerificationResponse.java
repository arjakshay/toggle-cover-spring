package com.togglecover.user.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.togglecover.user.entity.UserProfile;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserVerificationResponse {
    private String userId;
    private UserProfile.VerificationStatus verificationStatus;
    private UserProfile.KYCStatus kycStatus;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime verifiedAt;

    private String verifiedBy;
    private String verificationNotes;
}