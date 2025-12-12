package com.togglecover.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserSessionsResponse {
    private List<UserSession> sessions;
    private Integer totalActiveSessions;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UserSession {
        private String sessionId;
        private String deviceName;
        private String ipAddress;
        private String location;
        private LocalDateTime loginTime;
        private LocalDateTime lastActivity;
        private Boolean currentSession;
    }
}