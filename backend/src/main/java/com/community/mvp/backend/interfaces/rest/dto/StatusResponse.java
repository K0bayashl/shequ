package com.community.mvp.backend.interfaces.rest.dto;

import java.time.Instant;
import java.util.List;

public record StatusResponse(
    String application,
    List<String> activeProfiles,
    boolean databaseEnabled,
    boolean jwtEnabled,
    boolean redisEnabled,
    boolean openapiEnabled,
    Instant generatedAt
) {
}
