package com.community.mvp.backend.interfaces.rest.content.dto;

import java.util.List;

public record CommunityFeedResponse(
    List<CommunityThreadResponse> threads,
    List<TopicStatResponse> topics
) {
}
