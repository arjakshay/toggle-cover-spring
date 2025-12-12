package com.togglecover.user.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserProfileRequest {

    @NotBlank(message = "Full name is required")
    @Size(min = 2, max = 100, message = "Full name must be between 2 and 100 characters")
    private String fullName;

    @Email(message = "Invalid email format")
    @NotBlank(message = "Email is required")
    private String email;

    @NotBlank(message = "City is required")
    private String city;

    @NotNull(message = "Age is required")
    @Positive(message = "Age must be positive")
    private Integer age;

    @NotBlank(message = "Emergency contact is required")
    @Pattern(regexp = "^[+][0-9]{10,15}$", message = "Invalid emergency contact number")
    private String emergencyContact;

    @NotBlank(message = "Health declaration is required")
    @Size(max = 2000, message = "Health declaration cannot exceed 2000 characters")
    private String healthDeclaration;

    // Optional fields
    private String profilePictureUrl;
    private String occupation;
    private String companyName;
    private Double monthlyIncome;
    private String maritalStatus;
    private Integer dependents;
}