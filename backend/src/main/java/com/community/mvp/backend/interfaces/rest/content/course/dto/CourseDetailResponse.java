package com.community.mvp.backend.interfaces.rest.content.course.dto;

import java.util.List;

public record CourseDetailResponse(
    Long id,
    String title,
    String description,
    String coverImage,
    List<CourseChapterItemResponse> chapters
) {
}
