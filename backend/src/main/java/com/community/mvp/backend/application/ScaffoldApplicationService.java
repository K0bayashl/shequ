package com.community.mvp.backend.application;

import com.community.mvp.backend.config.CommunityMvpProperties;
import com.community.mvp.backend.domain.ScaffoldEcho;
import com.community.mvp.backend.domain.ScaffoldStatus;
import java.time.Instant;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class ScaffoldApplicationService {

    private final CommunityMvpProperties properties;

    public ScaffoldApplicationService(CommunityMvpProperties properties) {
        this.properties = properties;
    }

    public ScaffoldStatus buildStatus(List<String> activeProfiles) {
        return new ScaffoldStatus(
            "community-mvp-backend",
            activeProfiles,
            properties.getRuntime().isDatabaseEnabled(),
            properties.getSecurity().isJwtEnabled(),
            properties.getCache().isRedisEnabled(),
            properties.getDocs().isOpenapiEnabled(),
            Instant.now()
        );
    }

    public ScaffoldEcho echo(String message) {
        return new ScaffoldEcho(message.trim(), Instant.now());
    }
}
