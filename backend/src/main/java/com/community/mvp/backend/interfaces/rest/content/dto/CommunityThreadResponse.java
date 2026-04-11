package com.community.mvp.backend.interfaces.rest.content.dto;

import java.util.List;

public record CommunityThreadResponse(
    String id,
    String title,
    CommunityAuthorResponse author,
    String type,
    String timeAgo,
    int commentCount,
    List<String> tags,
    boolean isPinned
) {
}
