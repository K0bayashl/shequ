package com.community.mvp.backend.application.content.query;

public record GetChapterContentQuery(
    Long courseId,
    Long chapterId
) {
}
