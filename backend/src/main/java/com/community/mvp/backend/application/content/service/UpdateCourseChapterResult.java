package com.community.mvp.backend.application.content.service;

public record UpdateCourseChapterResult(
    Long courseId,
    Long chapterId,
    int sortOrder
) {
}