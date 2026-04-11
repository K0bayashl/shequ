package com.community.mvp.backend.interfaces.rest.content.course.dto;

public record CourseChapterItemResponse(
    Long id,
    String title,
    int sortOrder
) {
}
