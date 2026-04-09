package com.community.mvp.backend.interfaces.rest.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record EchoRequest(
    @NotBlank(message = "message must not be blank")
    @Size(max = 120, message = "message must not exceed 120 characters")
    String message
) {
}
