package com.community.mvp.backend.application.content.command;

public record UpdateCourseCommand(
    Long courseId,
    String title,
    String description,
    String coverImage,
    int status,
    Long operatorUserId
) {
}