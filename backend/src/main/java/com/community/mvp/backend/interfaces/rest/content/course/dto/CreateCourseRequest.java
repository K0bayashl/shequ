package com.community.mvp.backend.interfaces.rest.content.course.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import java.util.List;

public record CreateCourseRequest(
    @NotBlank(message = "title must not be blank")
    String title,
    @NotBlank(message = "description must not be blank")
    String description,
    String coverImage,
    @Min(value = 0, message = "status must be between 0 and 2")
    @Max(value = 2, message = "status must be between 0 and 2")
    int status,
    @Valid
    List<CreateCourseChapterRequest> chapters
) {
}
