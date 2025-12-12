package com.togglecover.auth.dto;

import com.togglecover.auth.entity.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuthResponse {
    private String token; // For backward compatibility
    private String accessToken;
    private String refreshToken;
    private String userId;
    private String phone;
    private String fullName;
    private User.UserType userType;
    private Long expiresIn;

    // Constructor for backward compatibility
    public AuthResponse(String token, String userId, String phone, String fullName,
                        User.UserType userType, Long expiresIn) {
        this.token = token;
        this.accessToken = token;
        this.userId = userId;
        this.phone = phone;
        this.fullName = fullName;
        this.userType = userType;
        this.expiresIn = expiresIn;
    }
}