package com.community.mvp.backend.interfaces.rest.moderation.dto;

import java.time.LocalDateTime;

public record ModerationReportResponse(
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
