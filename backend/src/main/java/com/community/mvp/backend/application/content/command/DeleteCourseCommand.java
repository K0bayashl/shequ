package com.community.mvp.backend.application.content.command;

public record DeleteCourseCommand(
    Long courseId,
    Long operatorUserId
) {
}