package com.community.mvp.backend.interfaces.rest.moderation.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record SubmitCourseReportRequest(
    @NotNull(message = "courseId must not be null")
    Long courseId,
    @NotBlank(message = "reasonCode must not be blank")
    String reasonCode,
    String reasonDetail
) {
}
