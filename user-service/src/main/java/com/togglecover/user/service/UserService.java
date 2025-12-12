package com.togglecover.user.service;

import com.togglecover.user.dto.*;
import com.togglecover.user.entity.UserProfile;
import com.togglecover.user.exception.UserProfileNotFoundException;
import com.togglecover.user.repository.UserProfileRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class UserService {

    private final UserProfileRepository userProfileRepository;
    private final AuthClientService authClientService;
    private final FileStorageService fileStorageService;
    private final AuditService auditService;
    private final UserMetricsService userMetricsService;

    @Transactional(readOnly = true)
    public UserProfileResponse getUserProfile(String userId) {
        log.info("Fetching profile for userId: {}", userId);

        return userProfileRepository.findByUserId(userId)
                .map(this::mapToResponse)
                .orElseGet(() -> {
                    log.info("No profile found for userId: {}, creating default profile", userId);
                    return createDefaultProfile(userId);
                });
    }

    public UserProfileResponse updateProfile(String userId, UserProfileRequest request) {
        log.info("Updating profile for userId: {}", userId);

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

        // Set additional fields from request if present
        if (request.getOccupation() != null) profile.setOccupation(request.getOccupation());
        if (request.getCompanyName() != null) profile.setCompanyName(request.getCompanyName());
        if (request.getMonthlyIncome() != null) profile.setMonthlyIncome(request.getMonthlyIncome());
        if (request.getMaritalStatus() != null) profile.setMaritalStatus(request.getMaritalStatus());
        if (request.getDependents() != null) profile.setDependents(request.getDependents());

        profile = userProfileRepository.save(profile);
        log.info("Profile saved for userId: {}", userId);

        // Update metrics
        userMetricsService.recordProfileUpdate(userId);

        return mapToResponse(profile);
    }

    @Transactional(readOnly = true)
    public boolean userExists(String userId) {
        return userProfileRepository.existsByUserId(userId);
    }

    @Transactional(readOnly = true)
    public Page<UserProfileResponse> getAllUsers(Pageable pageable, UserSearchRequest searchRequest) {
        log.info("Fetching users with pagination: {}", pageable);

        // For now, return all users - implement filtering later
        Page<UserProfile> userProfiles = userProfileRepository.findAll(pageable);

        return userProfiles.map(this::mapToResponse);
    }

    public void deleteProfile(String userId) {
        log.info("Deleting profile for userId: {}", userId);

        userProfileRepository.findByUserId(userId).ifPresent(profile -> {
            // Soft delete
            profile.setDeleted(true);
            profile.setDeletedAt(LocalDateTime.now());
            userProfileRepository.save(profile);

            // Audit deletion
            auditService.logAuditEvent(
                    userId,
                    "PROFILE_DELETION",
                    "User profile soft deleted",
                    Map.of("deletedAt", LocalDateTime.now())
            );
        });
    }

    public UserProfileResponse uploadProfilePicture(String userId, MultipartFile file) {
        log.info("Uploading profile picture for userId: {}", userId);

        UserProfile profile = userProfileRepository.findByUserId(userId)
                .orElseThrow(() -> new UserProfileNotFoundException("User profile not found"));

        try {
            // Upload file to storage
            String fileUrl = fileStorageService.uploadFile(file, "profile-pictures/" + userId);

            // Update profile with picture URL
            profile.setProfilePictureUrl(fileUrl);
            profile = userProfileRepository.save(profile);

            return mapToResponse(profile);

        } catch (IOException e) {
            log.error("Failed to upload profile picture for userId: {}", userId, e);
            throw new RuntimeException("Failed to upload profile picture");
        }
    }

    @Transactional(readOnly = true)
    public UserStatsResponse getUserStats(String userId) {
        log.info("Fetching stats for userId: {}", userId);

        UserProfile profile = userProfileRepository.findByUserId(userId)
                .orElseThrow(() -> new UserProfileNotFoundException("User profile not found"));

        // Get additional metrics
        UserMetrics metrics = userMetricsService.getUserMetrics(userId);

        return UserStatsResponse.builder()
                .userId(userId)
                .fullName(profile.getFullName())
                .totalLogins(metrics.getTotalLogins())
                .lastLogin(metrics.getLastLogin())
                .profileCompletion(getProfileCompletionPercentage(profile))
                .accountAgeInDays(calculateAccountAge(profile))
                .build();
    }

    public UserVerificationResponse verifyUser(String userId, VerificationRequest request) {
        log.info("Verifying user: {}", userId);

        UserProfile profile = userProfileRepository.findByUserId(userId)
                .orElseThrow(() -> new UserProfileNotFoundException("User profile not found"));

        // Update verification status
        profile.setVerificationStatus(request.getVerificationStatus());
        profile.setVerificationNotes(request.getVerificationNotes());
        profile.setVerifiedBy(request.getVerifiedBy());
        profile.setVerifiedAt(LocalDateTime.now());

        if (request.getVerificationStatus() == UserProfile.VerificationStatus.VERIFIED) {
            profile.setKycStatus(UserProfile.KYCStatus.COMPLETED);
        } else if (request.getVerificationStatus() == UserProfile.VerificationStatus.REJECTED) {
            profile.setKycStatus(UserProfile.KYCStatus.FAILED);
        }

        profile = userProfileRepository.save(profile);

        return UserVerificationResponse.builder()
                .userId(userId)
                .verificationStatus(profile.getVerificationStatus())
                .verifiedAt(profile.getVerifiedAt())
                .kycStatus(profile.getKycStatus())
                .verifiedBy(profile.getVerifiedBy())
                .verificationNotes(profile.getVerificationNotes())
                .build();
    }

    @Transactional(readOnly = true)
    public List<UserProfileResponse> searchUsers(UserSearchRequest searchRequest) {
        log.info("Searching users with criteria: {}", searchRequest);

        // For now, return all users - implement search later
        List<UserProfile> profiles = userProfileRepository.findAll();

        return profiles.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public UserPreferencesResponse updatePreferences(String userId, UserPreferencesRequest request) {
        log.info("Updating preferences for userId: {}", userId);

        UserProfile profile = userProfileRepository.findByUserId(userId)
                .orElseThrow(() -> new UserProfileNotFoundException("User profile not found"));

        // Update preferences
        profile.setNotificationPreferences(request.getNotificationPreferences());
        profile.setCommunicationPreferences(request.getCommunicationPreferences());
        profile.setPrivacySettings(request.getPrivacySettings());

        profile = userProfileRepository.save(profile);

        return UserPreferencesResponse.builder()
                .userId(userId)
                .notificationPreferences(profile.getNotificationPreferences())
                .communicationPreferences(profile.getCommunicationPreferences())
                .privacySettings(profile.getPrivacySettings())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    @Transactional(readOnly = true)
    public UserActivityResponse getUserActivity(String userId, LocalDateTime startDate, LocalDateTime endDate) {
        log.info("Fetching activity for userId: {} from {} to {}", userId, startDate, endDate);

        List<UserActivity> activities = userMetricsService.getUserActivities(userId, startDate, endDate);

        return UserActivityResponse.builder()
                .userId(userId)
                .activities(activities)
                .totalActivities(activities.size())
                .periodStart(startDate)
                .periodEnd(endDate)
                .build();
    }

    private UserProfileResponse createDefaultProfile(String userId) {
        try {
            // Try to get user info from auth service
            Map<String, Object> userInfo = authClientService.getUserInfo(userId);

            UserProfile defaultProfile = UserProfile.builder()
                    .userId(userId)
                    .fullName((String) userInfo.getOrDefault("fullName", "User"))
                    .email((String) userInfo.getOrDefault("email", ""))
                    .createdAt(LocalDateTime.now())
                    .updatedAt(LocalDateTime.now())
                    .verificationStatus(UserProfile.VerificationStatus.PENDING)
                    .kycStatus(UserProfile.KYCStatus.PENDING)
                    .isActive(true)
                    .deleted(false)
                    .build();

            userProfileRepository.save(defaultProfile);
            return mapToResponse(defaultProfile);

        } catch (Exception e) {
            log.warn("Could not fetch user info for userId: {}, creating empty profile", userId);

            UserProfile emptyProfile = UserProfile.builder()
                    .userId(userId)
                    .createdAt(LocalDateTime.now())
                    .updatedAt(LocalDateTime.now())
                    .verificationStatus(UserProfile.VerificationStatus.PENDING)
                    .kycStatus(UserProfile.KYCStatus.PENDING)
                    .isActive(true)
                    .deleted(false)
                    .build();

            userProfileRepository.save(emptyProfile);
            return mapToResponse(emptyProfile);
        }
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
                .profilePictureUrl(profile.getProfilePictureUrl())
                .verificationStatus(profile.getVerificationStatus())
                .kycStatus(profile.getKycStatus())
                .verificationNotes(profile.getVerificationNotes())
                .verifiedBy(profile.getVerifiedBy())
                .verifiedAt(profile.getVerifiedAt())
                .notificationPreferences(profile.getNotificationPreferences())
                .communicationPreferences(profile.getCommunicationPreferences())
                .privacySettings(profile.getPrivacySettings())
                .lastLogin(profile.getLastLogin())
                .occupation(profile.getOccupation())
                .companyName(profile.getCompanyName())
                .monthlyIncome(profile.getMonthlyIncome())
                .maritalStatus(profile.getMaritalStatus())
                .dependents(profile.getDependents())
                .isActive(profile.getIsActive())
                .createdAt(profile.getCreatedAt())
                .updatedAt(profile.getUpdatedAt())
                .build();
    }

    private double getProfileCompletionPercentage(UserProfile profile) {
        int totalFields = 7;
        int completedFields = 0;

        if (profile.getFullName() != null && !profile.getFullName().isEmpty()) completedFields++;
        if (profile.getEmail() != null && !profile.getEmail().isEmpty()) completedFields++;
        if (profile.getCity() != null && !profile.getCity().isEmpty()) completedFields++;
        if (profile.getAge() != null) completedFields++;
        if (profile.getEmergencyContact() != null && !profile.getEmergencyContact().isEmpty()) completedFields++;
        if (profile.getHealthDeclaration() != null && !profile.getHealthDeclaration().isEmpty()) completedFields++;
        if (profile.getProfilePictureUrl() != null && !profile.getProfilePictureUrl().isEmpty()) completedFields++;

        return (completedFields * 100.0) / totalFields;
    }

    private long calculateAccountAge(UserProfile profile) {
        if (profile.getCreatedAt() == null) return 0;
        return java.time.Duration.between(profile.getCreatedAt(), LocalDateTime.now()).toDays();
    }
}