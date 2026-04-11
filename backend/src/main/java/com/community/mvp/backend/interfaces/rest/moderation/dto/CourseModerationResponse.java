package com.community.mvp.backend.interfaces.rest.moderation.dto;

public record CourseModerationResponse(
    Long courseId,
    int status
) {
}
