package com.community.mvp.backend.application.moderation.service;

public record HandleCourseReportResult(
    Long reportId,
    int status,
    boolean courseTakenDown,
    boolean authorBanned
) {
}
