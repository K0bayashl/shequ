package com.community.mvp.backend.application.content.service;

import java.time.LocalDateTime;

public record CreateAdminCdkResult(
    Long cdkId,
    String code,
    LocalDateTime createdAt
) {
}