package com.community.mvp.backend.application.moderation.service;

public record CourseModerationResult(
    Long courseId,
    int status
) {
}
