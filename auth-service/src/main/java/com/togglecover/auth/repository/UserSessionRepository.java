package com.togglecover.auth.repository;

import com.togglecover.auth.entity.UserSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserSessionRepository extends JpaRepository<UserSession, String> {

    List<UserSession> findByUserIdAndActive(String userId, Boolean active);

    @Query("SELECT s FROM UserSession s WHERE s.userId = :userId AND s.active = true " +
            "ORDER BY s.lastActivity DESC")
    List<UserSession> findByUserIdAndActiveTrue(@Param("userId") String userId);

    Long countByUserIdAndActive(String userId, Boolean active);

    Optional<UserSession> findByIdAndUserId(String id, String userId);

    @Transactional
    @Modifying
    @Query("UPDATE UserSession s SET s.active = false, s.logoutTime = :logoutTime " +
            "WHERE s.userId = :userId AND s.active = true")
    void deactivateAllUserSessions(@Param("userId") String userId,
                                   @Param("logoutTime") LocalDateTime logoutTime);

    @Query("SELECT s FROM UserSession s WHERE s.lastActivity < :inactiveSince AND s.active = true")
    List<UserSession> findInactiveSessions(@Param("inactiveSince") LocalDateTime inactiveSince);
}