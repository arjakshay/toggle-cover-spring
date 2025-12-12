package com.togglecover.user.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuditService {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public void logAuditEvent(String userId, String eventType, String description, Map<String, Object> metadata) {
        try {
            Map<String, Object> auditEvent = Map.of(
                    "auditId", UUID.randomUUID().toString(),
                    "userId", userId,
                    "eventType", eventType,
                    "description", description,
                    "timestamp", LocalDateTime.now(),
                    "metadata", metadata,
                    "service", "user-service"
            );

            kafkaTemplate.send("audit-events", userId, auditEvent);
            log.debug("Audit event logged: {} for userId: {}", eventType, userId);

        } catch (Exception e) {
            log.error("Failed to log audit event for userId: {}", userId, e);
        }
    }
}