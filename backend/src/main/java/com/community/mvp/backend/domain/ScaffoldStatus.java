package com.community.mvp.backend.domain;

import java.time.Instant;
import java.util.List;

public record ScaffoldStatus(
    String application,
    List<String> activeProfiles,
    boolean databaseEnabled,
    boolean jwtEnabled,
    boolean redisEnabled,
    boolean openapiEnabled,
    Instant generatedAt
) {
}
