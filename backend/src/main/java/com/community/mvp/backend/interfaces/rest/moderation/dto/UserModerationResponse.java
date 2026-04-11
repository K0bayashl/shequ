package com.community.mvp.backend.interfaces.rest.moderation.dto;

public record UserModerationResponse(
    Long userId,
    int status
) {
}
