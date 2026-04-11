package com.community.mvp.backend.interfaces.rest.content.dto;

public record AdminCdkItemResponse(
    long id,
    String key,
    String status,
    String usedBy,
    String usedByEmail,
    String usedAt,
    String createdAt
) {
}
