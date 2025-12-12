package com.togglecover.user.service;

import com.togglecover.user.dto.UserActivity;
import com.togglecover.user.dto.UserMetrics;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserMetricsService {

    private final RedisTemplate<String, Object> redisTemplate;

    public void recordProfileUpdate(String userId) {
        String key = "user:metrics:" + userId + ":updates";
        redisTemplate.opsForValue().increment(key, 1);
        redisTemplate.expire(key, 30, TimeUnit.DAYS);

        // Also record activity
        recordActivity(userId, "PROFILE_UPDATE", "User updated profile");
    }

    public void recordLogin(String userId) {
        String key = "user:metrics:" + userId + ":logins";
        redisTemplate.opsForValue().increment(key, 1);

        // Store last login time
        String lastLoginKey = "user:metrics:" + userId + ":lastLogin";
        redisTemplate.opsForValue().set(lastLoginKey, LocalDateTime.now().toString());
        redisTemplate.expire(lastLoginKey, 30, TimeUnit.DAYS);

        // Record activity
        recordActivity(userId, "LOGIN", "User logged in");
    }

    public UserMetrics getUserMetrics(String userId) {
        String loginKey = "user:metrics:" + userId + ":logins";
        String lastLoginKey = "user:metrics:" + userId + ":lastLogin";
        String updateKey = "user:metrics:" + userId + ":updates";

        Integer totalLogins = (Integer) redisTemplate.opsForValue().get(loginKey);
        String lastLoginStr = (String) redisTemplate.opsForValue().get(lastLoginKey);
        Integer totalUpdates = (Integer) redisTemplate.opsForValue().get(updateKey);

        LocalDateTime lastLogin = null;
        if (lastLoginStr != null) {
            lastLogin = LocalDateTime.parse(lastLoginStr);
        }

        return UserMetrics.builder()
                .totalLogins(totalLogins != null ? totalLogins : 0)
                .lastLogin(lastLogin)
                .totalUpdates(totalUpdates != null ? totalUpdates : 0)
                .build();
    }

    public List<UserActivity> getUserActivities(String userId, LocalDateTime startDate, LocalDateTime endDate) {
        String activityKey = "user:activity:" + userId;

        // In a real implementation, you would query from a database
        // For now, return some dummy activities
        List<UserActivity> activities = new ArrayList<>();

        activities.add(UserActivity.builder()
                .activityId("1")
                .activityType("PROFILE_UPDATE")
                .description("Updated profile information")
                .timestamp(LocalDateTime.now().minusHours(2))
                .build());

        activities.add(UserActivity.builder()
                .activityId("2")
                .activityType("LOGIN")
                .description("User logged in from mobile device")
                .timestamp(LocalDateTime.now().minusDays(1))
                .build());

        activities.add(UserActivity.builder()
                .activityId("3")
                .activityType("PREFERENCE_UPDATE")
                .description("Updated notification preferences")
                .timestamp(LocalDateTime.now().minusDays(3))
                .build());

        // Filter by date range
        return activities.stream()
                .filter(activity -> activity.getTimestamp().isAfter(startDate)
                        && activity.getTimestamp().isBefore(endDate))
                .collect(Collectors.toList());
    }

    private void recordActivity(String userId, String activityType, String description) {
        try {
            String activityKey = "user:activity:" + userId + ":" + System.currentTimeMillis();

            UserActivity activity = UserActivity.builder()
                    .activityId(UUID.randomUUID().toString())
                    .activityType(activityType)
                    .description(description)
                    .timestamp(LocalDateTime.now())
                    .build();

            // Store in Redis with 90 days TTL
            redisTemplate.opsForValue().set(activityKey, activity, 90, TimeUnit.DAYS);

            log.debug("Activity recorded: {} for userId: {}", activityType, userId);

        } catch (Exception e) {
            log.error("Failed to record activity for userId: {}", userId, e);
        }
    }
}