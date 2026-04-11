package com.community.mvp.backend.application.user.service;

import com.community.mvp.backend.domain.user.model.UserProfile;

public record AuthenticationResult(
    String token,
    UserProfile user
) {
}

