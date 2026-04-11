package com.community.mvp.backend.domain.content.model;

import java.time.LocalDateTime;

public record CourseChapter(
    Long id,
    Long courseId,
    String title,
    String content,
    int sortOrder,
    int moderationStatus,
    String moderationReason,
    Long moderatedBy,
    LocalDateTime moderatedAt,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {
}
