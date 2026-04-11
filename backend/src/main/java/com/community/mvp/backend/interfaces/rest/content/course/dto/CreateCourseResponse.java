package com.community.mvp.backend.interfaces.rest.content.course.dto;

public record CreateCourseResponse(
    Long courseId,
    int status,
    int chapterCount
) {
}
