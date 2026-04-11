package com.community.mvp.backend.application.user.query;

public record GetViewerProfileQuery(
    Long viewerId,
    Long targetUserId
) {
}
