package com.togglecover.user.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserMetrics {
    private Integer totalLogins;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime lastLogin;

    private Integer totalUpdates;
    private Integer totalActivities;
    private Double profileCompletion;
    private Long accountAgeInDays;

    // Additional metrics
    private Integer totalCoverageToggles;
    private Integer totalClaims;
    private Double averageSessionDuration; // in minutes
    private Integer activeDaysInLast30;
}