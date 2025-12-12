package com.togglecover.user.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserPreferencesRequest {
    @NotNull(message = "Notification preferences are required")
    private Map<String, Object> notificationPreferences;

    @NotNull(message = "Communication preferences are required")
    private Map<String, Object> communicationPreferences;

    @NotNull(message = "Privacy settings are required")
    private Map<String, Object> privacySettings;
}