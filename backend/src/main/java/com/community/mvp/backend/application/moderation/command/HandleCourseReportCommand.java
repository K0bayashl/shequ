package com.community.mvp.backend.application.moderation.command;

public record HandleCourseReportCommand(
    Long reportId,
    String decision,
    String handleNote,
    boolean takedownCourse,
    boolean banAuthor,
    Long handlerUserId
) {
}
