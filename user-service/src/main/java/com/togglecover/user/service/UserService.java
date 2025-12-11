package com.togglecover.user.service;

import com.togglecover.user.dto.UserProfileRequest;
import com.togglecover.user.dto.UserProfileResponse;
import com.togglecover.user.entity.UserProfile;
import com.togglecover.user.exception.UserProfileNotFoundException;
import com.togglecover.user.repository.UserProfileRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class UserService {

    private final UserProfileRepository userProfileRepository;

    @Transactional(readOnly = true)
    public UserProfileResponse getUserProfile(String userId) {
        // Try to get existing profile
        return userProfileRepository.findByUserId(userId)
                .map(this::mapToResponse)
                .orElseGet(() -> {
                    log.info("No profile found for userId: {}, returning empty profile", userId);
                    // Return empty/default profile instead of throwing exception
                    return UserProfileResponse.builder()
                            .userId(userId)
                            .fullName(null)
                            .email(null)
                            .city(null)
                            .age(null)
                            .emergencyContact(null)
                            .healthDeclaration(null)
                            .createdAt(null)
                            .updatedAt(null)
                            .build();
                });
    }

    public UserProfileResponse updateProfile(String userId, UserProfileRequest request) {
        UserProfile profile = userProfileRepository.findByUserId(userId)
                .orElseGet(() -> {
                    log.info("Creating new profile for userId: {}", userId);
                    return UserProfile.builder()
                            .userId(userId)
                            .build();
                });

        // Update profile fields
        profile.setFullName(request.getFullName());
        profile.setEmail(request.getEmail());
        profile.setCity(request.getCity());
        profile.setAge(request.getAge());
        profile.setEmergencyContact(request.getEmergencyContact());
        profile.setHealthDeclaration(request.getHealthDeclaration());

        profile = userProfileRepository.save(profile);
        log.info("Profile saved for userId: {}", userId);

        return mapToResponse(profile);
    }

    @Cacheable(value = "userExists", key = "#userId")
    @Transactional(readOnly = true)
    public boolean userExists(String userId) {
        return userProfileRepository.existsByUserId(userId);
    }

    private UserProfileResponse mapToResponse(UserProfile profile) {
        return UserProfileResponse.builder()
                .userId(profile.getUserId())
                .fullName(profile.getFullName())
                .email(profile.getEmail())
                .city(profile.getCity())
                .age(profile.getAge())
                .emergencyContact(profile.getEmergencyContact())
                .healthDeclaration(profile.getHealthDeclaration())
                .createdAt(profile.getCreatedAt())
                .updatedAt(profile.getUpdatedAt())
                .build();
    }
}