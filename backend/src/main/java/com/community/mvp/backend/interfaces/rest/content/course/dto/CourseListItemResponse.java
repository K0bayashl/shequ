package com.community.mvp.backend.interfaces.rest.content.course.dto;

import java.time.LocalDateTime;

public record CourseListItemResponse(
    Long id,
    String title,
    String description,
    String coverImage,
    int chapterCount,
    LocalDateTime publishedAt
) {
}
