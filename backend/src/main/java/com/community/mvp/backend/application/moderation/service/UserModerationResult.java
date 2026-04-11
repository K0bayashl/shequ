package com.community.mvp.backend.application.moderation.service;

public record UserModerationResult(
    Long userId,
    int status
) {
}
