package com.community.mvp.backend.interfaces.rest.content.course.dto;

public record ChapterContentResponse(
    Long courseId,
    Long chapterId,
    String title,
    int sortOrder,
    String content
) {
}
