package com.togglecover.user.repository;

import com.togglecover.user.entity.UserProfile;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserProfileRepository extends JpaRepository<UserProfile, String> {

    Optional<UserProfile> findByUserId(String userId);

    boolean existsByUserId(String userId);

    List<UserProfile> findByCity(String city);

    List<UserProfile> findByAgeBetween(Integer minAge, Integer maxAge);

    List<UserProfile> findByVerificationStatus(UserProfile.VerificationStatus status);

    List<UserProfile> findByKycStatus(UserProfile.KYCStatus status);

    @Query("SELECT up FROM UserProfile up WHERE " +
            "(:name IS NULL OR LOWER(up.fullName) LIKE LOWER(CONCAT('%', :name, '%'))) AND " +
            "(:city IS NULL OR LOWER(up.city) = LOWER(:city)) AND " +
            "(:minAge IS NULL OR up.age >= :minAge) AND " +
            "(:maxAge IS NULL OR up.age <= :maxAge) AND " +
            "up.deleted = false")
    Page<UserProfile> findAllWithFilters(
            @Param("name") String name,
            @Param("city") String city,
            @Param("minAge") Integer minAge,
            @Param("maxAge") Integer maxAge,
            Pageable pageable);

    @Query("SELECT up FROM UserProfile up WHERE " +
            "(:name IS NULL OR LOWER(up.fullName) LIKE LOWER(CONCAT('%', :name, '%'))) AND " +
            "(:city IS NULL OR LOWER(up.city) = LOWER(:city)) AND " +
            "(:minAge IS NULL OR up.age >= :minAge) AND " +
            "(:maxAge IS NULL OR up.age <= :maxAge) AND " +
            "(:verificationStatus IS NULL OR up.verificationStatus = :verificationStatus) AND " +
            "(:kycStatus IS NULL OR up.kycStatus = :kycStatus) AND " +
            "up.deleted = false")
    List<UserProfile> searchUsers(
            @Param("name") String name,
            @Param("city") String city,
            @Param("minAge") Integer minAge,
            @Param("maxAge") Integer maxAge,
            @Param("verificationStatus") UserProfile.VerificationStatus verificationStatus,
            @Param("kycStatus") UserProfile.KYCStatus kycStatus);

    @Query("SELECT COUNT(up) FROM UserProfile up WHERE up.createdAt >= :startDate AND up.createdAt <= :endDate")
    Long countByRegistrationDateRange(@Param("startDate") LocalDateTime startDate,
                                      @Param("endDate") LocalDateTime endDate);

    @Query("SELECT up.city, COUNT(up) FROM UserProfile up WHERE up.deleted = false GROUP BY up.city")
    List<Object[]> getUsersByCity();

    @Query("SELECT up FROM UserProfile up WHERE up.lastLogin IS NOT NULL ORDER BY up.lastLogin DESC LIMIT 10")
    List<UserProfile> findRecentActiveUsers();

    @Query("SELECT up FROM UserProfile up WHERE up.verificationStatus = 'PENDING' AND up.deleted = false")
    List<UserProfile> findPendingVerifications();
}