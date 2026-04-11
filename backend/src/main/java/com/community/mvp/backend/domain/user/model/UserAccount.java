package com.community.mvp.backend.domain.user.model;

import java.time.Instant;
import java.time.LocalDateTime;

public record UserAccount(
    Long id,
    String username,
    String email,
    String passwordHash,
    String avatar,
    UserRole role,
    UserStatus status,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {

    public UserAccount withPasswordHash(String passwordHash) {
        return new UserAccount(id, username, email, passwordHash, avatar, role, status, createdAt, updatedAt);
    }

    public UserAccount withStatus(UserStatus status) {
        return new UserAccount(id, username, email, passwordHash, avatar, role, status, createdAt, updatedAt);
    }

    public UserProfile toProfile() {
        return new UserProfile(
            id,
            username,
            email,
            avatar,
            role.getCode(),
            status.getCode(),
            createdAt
        );
    }

    public UserPrincipal toPrincipal(Instant issuedAt) {
        return new UserPrincipal(
            id,
            username,
            email,
            role.getCode(),
            status.getCode(),
            issuedAt
        );
    }
}
