package com.community.mvp.backend.interfaces.rest.content.dto;

public record CommunityAuthorResponse(
    String name,
    String avatar,
    String initials
) {
}
