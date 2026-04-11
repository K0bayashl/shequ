package com.community.mvp.backend.interfaces.rest.moderation.dto;

import jakarta.validation.constraints.NotBlank;

public record HandleCourseReportRequest(
    @NotBlank(message = "decision must not be blank")
    String decision,
    String handleNote,
    boolean takedownCourse,
    boolean banAuthor
) {
}
