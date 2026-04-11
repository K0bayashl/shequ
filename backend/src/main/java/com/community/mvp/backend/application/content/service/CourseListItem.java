package com.community.mvp.backend.application.content.service;

import java.time.LocalDateTime;

public record CourseListItem(
    Long id,
    String title,
    String description,
    String coverImage,
    int chapterCount,
    LocalDateTime publishedAt
) {
}
