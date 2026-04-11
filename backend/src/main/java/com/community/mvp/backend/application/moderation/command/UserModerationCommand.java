package com.community.mvp.backend.application.moderation.command;

public record UserModerationCommand(
    Long userId,
    String reason,
    Long handlerUserId
) {
}
