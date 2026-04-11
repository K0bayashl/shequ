package com.community.mvp.backend.interfaces.rest.user.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record RegisterRequest(
    @NotBlank(message = "username must not be blank")
    String username,
    @NotBlank(message = "email must not be blank")
    @Email(message = "email must be a valid email address")
    String email,
    @NotBlank(message = "password must not be blank")
    String password,
    @NotBlank(message = "confirmPassword must not be blank")
    String confirmPassword,
    @NotBlank(message = "cdkCode must not be blank")
    String cdkCode
) {
}

