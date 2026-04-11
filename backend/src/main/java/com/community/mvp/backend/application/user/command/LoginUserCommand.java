package com.community.mvp.backend.application.user.command;

public record LoginUserCommand(
    String email,
    String password
) {
}
