package com.community.mvp.backend.application.content.service;

public record CreateCourseResult(
    Long courseId,
    int status,
    int chapterCount
) {
}
