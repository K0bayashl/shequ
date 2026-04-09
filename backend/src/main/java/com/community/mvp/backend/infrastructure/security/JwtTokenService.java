package com.community.mvp.backend.infrastructure.security;

import com.community.mvp.backend.config.CommunityMvpProperties;
import java.util.Optional;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
public class JwtTokenService {

    private static final String BEARER_PREFIX = "Bearer ";

    private final CommunityMvpProperties properties;

    public JwtTokenService(CommunityMvpProperties properties) {
        this.properties = properties;
    }

    public Optional<String> resolveBearerToken(String authorizationHeader) {
        if (!properties.getSecurity().isJwtEnabled()) {
            return Optional.empty();
        }

        if (!StringUtils.hasText(authorizationHeader) || !authorizationHeader.startsWith(BEARER_PREFIX)) {
            return Optional.empty();
        }

        String token = authorizationHeader.substring(BEARER_PREFIX.length()).trim();
        return StringUtils.hasText(token) ? Optional.of(token) : Optional.empty();
    }
}
