package com.community.mvp.backend.application.user.command;

public record ChangePasswordCommand(
    Long userId,
    String oldPassword,
    String newPassword,
    String confirmNewPassword
) {
}
