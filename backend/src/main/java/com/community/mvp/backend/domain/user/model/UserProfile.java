package com.community.mvp.backend.domain.user.model;

import java.time.LocalDateTime;

public record UserProfile(
    Long id,
    String username,
    String email,
    String avatar,
    int role,
    int status,
    LocalDateTime createdAt
) {
}

