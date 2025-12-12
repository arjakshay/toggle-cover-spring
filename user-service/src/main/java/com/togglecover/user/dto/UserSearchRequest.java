package com.togglecover.user.dto;

import com.togglecover.user.entity.UserProfile;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserSearchRequest {
    private String name;
    private String email;
    private String city;
    private Integer minAge;
    private Integer maxAge;
    private UserProfile.VerificationStatus verificationStatus;
    private UserProfile.KYCStatus kycStatus;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private LocalDateTime createdAfter;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private LocalDateTime createdBefore;

    private Boolean active;
    private String sortBy;
    private String sortDirection;
}