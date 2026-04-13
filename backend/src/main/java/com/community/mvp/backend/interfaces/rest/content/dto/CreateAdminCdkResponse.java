package com.community.mvp.backend.interfaces.rest.content.dto;

public record CreateAdminCdkResponse(
    Long id,
    String key,
    String status,
    String createdAt
) {
}