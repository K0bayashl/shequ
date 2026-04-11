package com.community.mvp.backend.interfaces.rest.content.course.dto;

public record UpdateCourseResponse(
    Long courseId,
    int status
) {
}