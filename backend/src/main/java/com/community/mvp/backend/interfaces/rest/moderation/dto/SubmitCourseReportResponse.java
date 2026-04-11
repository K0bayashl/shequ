package com.community.mvp.backend.interfaces.rest.moderation.dto;

public record SubmitCourseReportResponse(
    Long reportId,
    int status
) {
}
