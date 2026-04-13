package com.community.mvp.backend.interfaces.rest.content.dto;

import jakarta.validation.constraints.NotBlank;

public record CreateAdminCdkRequest(
    @NotBlank(message = "code must not be blank")
    String code
) {
}