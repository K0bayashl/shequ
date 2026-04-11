package com.community.mvp.backend.application.moderation.command;

public record CourseModerationCommand(
    Long courseId,
    String reason,
    Long handlerUserId
) {
}
