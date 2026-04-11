package com.community.mvp.backend.application.content.service;

import java.util.List;

public record CourseDetailResult(
    Long id,
    String title,
    String description,
    String coverImage,
    List<CourseChapterItem> chapters
) {
}
