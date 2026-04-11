package com.community.mvp.backend.application.content.command;

public record CreateCourseChapterCommand(
    String title,
    String content,
    int sortOrder
) {
}
