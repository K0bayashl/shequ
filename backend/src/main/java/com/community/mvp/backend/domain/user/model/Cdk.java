package com.community.mvp.backend.domain.user.model;

import java.time.LocalDateTime;

public record Cdk(
    Long id,
    String code,
    boolean used,
    Long usedBy,
    LocalDateTime usedAt,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {

    public Cdk markUsed(Long userId, LocalDateTime usedAt) {
        return new Cdk(id, code, true, userId, usedAt, createdAt, updatedAt);
    }
}
