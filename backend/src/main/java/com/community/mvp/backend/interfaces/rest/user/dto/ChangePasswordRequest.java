package com.community.mvp.backend.interfaces.rest.user.dto;

import jakarta.validation.constraints.NotBlank;

public record ChangePasswordRequest(
    @NotBlank(message = "oldPassword must not be blank")
    String oldPassword,
    @NotBlank(message = "newPassword must not be blank")
    String newPassword,
    @NotBlank(message = "confirmNewPassword must not be blank")
    String confirmNewPassword
) {
}

