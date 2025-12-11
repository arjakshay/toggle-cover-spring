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
    private String token;
    private String userId;
    private String phone;
    private String fullName;
    private User.UserType userType;
    private Long expiresIn;
}