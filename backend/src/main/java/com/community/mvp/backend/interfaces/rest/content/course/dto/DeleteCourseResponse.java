package com.community.mvp.backend.interfaces.rest.content.course.dto;

public record DeleteCourseResponse(
    Long courseId,
    int status
) {
}