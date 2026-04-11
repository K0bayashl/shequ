package com.community.mvp.backend.application.moderation.command;

public record SubmitCourseReportCommand(
    Long courseId,
    String reasonCode,
    String reasonDetail,
    Long reporterUserId
) {
}
