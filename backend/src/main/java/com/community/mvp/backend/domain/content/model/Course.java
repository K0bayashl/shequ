package com.community.mvp.backend.domain.content.model;

import java.time.LocalDateTime;

public record Course(
    Long id,
    String title,
    String description,
    String coverImage,
    CourseStatus status,
    int moderationStatus,
    String moderationReason,
    Long moderatedBy,
    LocalDateTime moderatedAt,
    Long createdBy,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {
}
