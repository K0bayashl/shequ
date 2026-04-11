package com.community.mvp.backend.domain.moderation.model;

import java.time.LocalDateTime;

public record ContentReport(
    Long id,
    ReportContentType contentType,
    Long contentId,
    Long reporterUserId,
    String reasonCode,
    String reasonDetail,
    ReportStatus status,
    Long handledBy,
    LocalDateTime handledAt,
    String handleNote,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {

    public ContentReport resolve(Long handlerUserId, String note, LocalDateTime handledTime) {
        return new ContentReport(
            id,
            contentType,
            contentId,
            reporterUserId,
            reasonCode,
            reasonDetail,
            ReportStatus.RESOLVED,
            handlerUserId,
            handledTime,
            note,
            createdAt,
            updatedAt
        );
    }

    public ContentReport reject(Long handlerUserId, String note, LocalDateTime handledTime) {
        return new ContentReport(
            id,
            contentType,
            contentId,
            reporterUserId,
            reasonCode,
            reasonDetail,
            ReportStatus.REJECTED,
            handlerUserId,
            handledTime,
            note,
            createdAt,
            updatedAt
        );
    }
}
