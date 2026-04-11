package com.community.mvp.backend.domain.moderation.model;

import java.time.LocalDateTime;

public record ActionAuditLog(
    Long id,
    Long actorUserId,
    String actionType,
    String targetType,
    Long targetId,
    String actionResult,
    String detailJson,
    LocalDateTime createdAt
) {
}
