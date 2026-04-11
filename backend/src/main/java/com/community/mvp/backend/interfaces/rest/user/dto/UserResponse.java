package com.community.mvp.backend.interfaces.rest.user.dto;

import java.time.LocalDateTime;

public record UserResponse(
    Long id,
    String username,
    String email,
    String avatar,
    int role,
    int status,
    LocalDateTime createdAt
) {
}

