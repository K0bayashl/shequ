package com.community.mvp.backend.application.moderation.service;

import java.time.LocalDateTime;

public record ModerationReportItem(
    Long reportId,
    String contentType,
    Long contentId,
    Long reporterUserId,
    String reasonCode,
    String reasonDetail,
    int status,
    Long handledBy,
    LocalDateTime handledAt,
    String handleNote,
    LocalDateTime createdAt
) {
}
