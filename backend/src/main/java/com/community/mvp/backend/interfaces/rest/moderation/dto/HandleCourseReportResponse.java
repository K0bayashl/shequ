package com.community.mvp.backend.interfaces.rest.moderation.dto;

public record HandleCourseReportResponse(
    Long reportId,
    int status,
    boolean courseTakenDown,
    boolean authorBanned
) {
}
