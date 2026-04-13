package com.community.mvp.backend.application.content.command;

public record UpdateCourseChapterCommand(
    Long courseId,
    Long chapterId,
    String title,
    String content,
    int sortOrder,
    Long operatorUserId
) {
}