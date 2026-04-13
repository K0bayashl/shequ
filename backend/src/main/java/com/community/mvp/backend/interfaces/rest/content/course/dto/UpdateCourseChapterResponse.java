package com.community.mvp.backend.interfaces.rest.content.course.dto;

public record UpdateCourseChapterResponse(
    Long courseId,
    Long chapterId,
    int sortOrder
) {
}