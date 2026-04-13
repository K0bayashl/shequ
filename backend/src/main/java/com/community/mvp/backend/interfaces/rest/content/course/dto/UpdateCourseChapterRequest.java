package com.community.mvp.backend.interfaces.rest.content.course.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

public record UpdateCourseChapterRequest(
    @NotBlank(message = "title must not be blank")
    String title,
    @NotBlank(message = "content must not be blank")
    String content,
    @Min(value = 1, message = "sortOrder must be greater than 0")
    int sortOrder
) {
}