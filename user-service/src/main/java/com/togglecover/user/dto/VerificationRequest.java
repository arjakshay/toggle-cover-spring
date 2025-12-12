package com.togglecover.user.dto;

import com.togglecover.user.entity.UserProfile;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VerificationRequest {
    @NotNull(message = "Verification status is required")
    private UserProfile.VerificationStatus verificationStatus;

    private String verificationNotes;

    @NotBlank(message = "Verified by is required")
    private String verifiedBy;

    private String rejectionReason;
}