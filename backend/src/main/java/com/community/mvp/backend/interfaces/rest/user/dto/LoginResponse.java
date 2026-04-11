package com.community.mvp.backend.interfaces.rest.user.dto;

public record LoginResponse(
    String token,
    UserResponse user
) {
}

