package com.community.mvp.backend.application.content.service;

public record ChapterContentResult(
    Long courseId,
    Long chapterId,
    String title,
    int sortOrder,
    String content
) {
}
