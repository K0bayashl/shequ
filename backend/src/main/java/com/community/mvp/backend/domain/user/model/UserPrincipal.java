package com.community.mvp.backend.domain.user.model;

import java.time.Instant;

public record UserPrincipal(
    Long userId,
    String username,
    String email,
    Integer role,
    Integer status,
    Instant issuedAt
) {
}

