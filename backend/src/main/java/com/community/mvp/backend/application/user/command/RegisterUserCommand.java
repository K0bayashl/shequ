package com.community.mvp.backend.application.user.command;

public record RegisterUserCommand(
    String username,
    String email,
    String password,
    String confirmPassword,
    String cdkCode
) {
}
