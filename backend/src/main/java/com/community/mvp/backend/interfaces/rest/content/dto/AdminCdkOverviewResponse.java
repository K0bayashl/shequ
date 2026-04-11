package com.community.mvp.backend.interfaces.rest.content.dto;

import java.util.List;

public record AdminCdkOverviewResponse(
    int totalUsers,
    int activeMembers,
    int remainingCdks,
    List<AdminCdkItemResponse> cdks
) {
}
