package com.togglecover.auth.repository;

import com.togglecover.auth.entity.Otp;
import com.togglecover.auth.entity.OtpType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface OtpRepository extends JpaRepository<Otp, String> {

    @Query("SELECT o FROM Otp o WHERE o.phone = :phone AND o.otp = :otp " +
            "AND o.type = :type AND o.expiresAt > :now AND o.isUsed = false")
    Optional<Otp> findValidOtp(@Param("phone") String phone,
                               @Param("otp") String otp,
                               @Param("type") OtpType type,
                               @Param("now") LocalDateTime now);

    @Query("SELECT o FROM Otp o WHERE o.phone = :phone AND o.type = :type " +
            "AND o.expiresAt > :now AND o.isUsed = false ORDER BY o.createdAt DESC")
    Optional<Otp> findLatestValidOtp(@Param("phone") String phone,
                                     @Param("type") OtpType type,
                                     @Param("now") LocalDateTime now);

    @Transactional
    @Modifying
    @Query("DELETE FROM Otp o WHERE o.phone = :phone AND o.type = :type AND o.isUsed = false")
    void deleteByPhoneAndTypeAndExpired(@Param("phone") String phone,
                                        @Param("type") OtpType type);

    @Transactional
    @Modifying
    @Query("DELETE FROM Otp o WHERE o.expiresAt < :cutoff")
    void deleteExpiredOtps(@Param("cutoff") LocalDateTime cutoff);

    @Query("SELECT COUNT(o) FROM Otp o WHERE o.phone = :phone " +
            "AND o.createdAt > :startTime AND o.type = :type")
    Long countRecentOtps(@Param("phone") String phone,
                         @Param("type") OtpType type,
                         @Param("startTime") LocalDateTime startTime);
}