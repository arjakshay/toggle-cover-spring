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
public class UserStatsResponse {
    private String userId;
    private String fullName;
    private Integer totalLogins;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime lastLogin;

    private Double profileCompletion;
    private Long accountAgeInDays;
    private Integer totalActivities;
    private Integer totalUpdates;
}