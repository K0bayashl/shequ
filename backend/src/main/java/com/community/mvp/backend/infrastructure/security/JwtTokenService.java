package com.community.mvp.backend.infrastructure.security;

import com.community.mvp.backend.config.CommunityMvpProperties;
import com.community.mvp.backend.domain.user.model.UserPrincipal;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Clock;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
public class JwtTokenService {

    private static final String BEARER_PREFIX = "Bearer ";
    private static final String HMAC_ALGORITHM = "HmacSHA256";
    private static final Base64.Encoder BASE64_URL_ENCODER = Base64.getUrlEncoder().withoutPadding();
    private static final Base64.Decoder BASE64_URL_DECODER = Base64.getUrlDecoder();
    private static final TypeReference<Map<String, Object>> MAP_TYPE = new TypeReference<>() {};
    private static final long TOKEN_TTL_MINUTES = 60L * 24L * 7L;

    private final CommunityMvpProperties properties;
    private final ObjectMapper objectMapper;
    private final Clock clock;

    public JwtTokenService(CommunityMvpProperties properties, ObjectMapper objectMapper) {
        this.properties = properties;
        this.objectMapper = objectMapper;
        this.clock = Clock.systemUTC();
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

    public String issueToken(UserPrincipal principal) {
        Instant issuedAt = Instant.now(clock);
        Instant expiresAt = issuedAt.plus(TOKEN_TTL_MINUTES, ChronoUnit.MINUTES);
        Map<String, Object> claims = new LinkedHashMap<>();
        claims.put("sub", principal.userId().toString());
        claims.put("userId", principal.userId());
        claims.put("username", principal.username());
        claims.put("email", principal.email());
        claims.put("role", principal.role());
        claims.put("status", principal.status());
        claims.put("iat", issuedAt.toEpochMilli());
        claims.put("exp", expiresAt.toEpochMilli());
        return sign(claims);
    }

    public Optional<UserPrincipal> parseAndValidate(String token) {
        if (!StringUtils.hasText(token)) {
            return Optional.empty();
        }

        String[] parts = token.split("\\.");
        if (parts.length != 3) {
            return Optional.empty();
        }

        byte[] expectedSignature = hmac(parts[0] + "." + parts[1]);
        byte[] actualSignature;
        try {
            actualSignature = BASE64_URL_DECODER.decode(parts[2]);
        } catch (IllegalArgumentException exception) {
            return Optional.empty();
        }
        if (!MessageDigest.isEqual(expectedSignature, actualSignature)) {
            return Optional.empty();
        }

        Map<String, Object> claims;
        try {
            byte[] payloadBytes = BASE64_URL_DECODER.decode(parts[1]);
            claims = objectMapper.readValue(payloadBytes, MAP_TYPE);
        } catch (Exception exception) {
            return Optional.empty();
        }

        Instant now = Instant.now(clock);
        Long expMillis = asLongObject(claims.get("exp"));
        Long iatMillis = asLongObject(claims.get("iat"));
        if (expMillis == null || iatMillis == null) {
            return Optional.empty();
        }
        Instant expiresAt = Instant.ofEpochMilli(expMillis);
        if (now.isAfter(expiresAt)) {
            return Optional.empty();
        }

        Long userId = asLongObject(claims.get("userId"));
        if (userId == null) {
            return Optional.empty();
        }

        String username = asString(claims.get("username"));
        String email = asString(claims.get("email"));
        Integer role = asInteger(claims.get("role"));
        Integer status = asInteger(claims.get("status"));
        Instant issuedAt = Instant.ofEpochMilli(iatMillis);
        return Optional.of(new UserPrincipal(userId, username, email, role, status, issuedAt));
    }

    public Optional<UserPrincipal> parseBearerToken(String authorizationHeader) {
        return resolveBearerToken(authorizationHeader)
            .flatMap(this::parseAndValidate);
    }

    private String sign(Map<String, Object> claims) {
        if (!StringUtils.hasText(properties.getSecurity().getJwtSecret())) {
            throw new IllegalStateException("JWT secret must not be blank when JWT is enabled.");
        }
        try {
            String header = BASE64_URL_ENCODER.encodeToString(
                objectMapper.writeValueAsBytes(Map.of("alg", "HS256", "typ", "JWT"))
            );
            String payload = BASE64_URL_ENCODER.encodeToString(objectMapper.writeValueAsBytes(claims));
            String signingInput = header + "." + payload;
            String signature = BASE64_URL_ENCODER.encodeToString(hmac(signingInput));
            return signingInput + "." + signature;
        } catch (Exception exception) {
            throw new IllegalStateException("Failed to issue JWT token.", exception);
        }
    }

    private byte[] hmac(String signingInput) {
        try {
            Mac mac = Mac.getInstance(HMAC_ALGORITHM);
            mac.init(new SecretKeySpec(properties.getSecurity().getJwtSecret().getBytes(StandardCharsets.UTF_8), HMAC_ALGORITHM));
            return mac.doFinal(signingInput.getBytes(StandardCharsets.UTF_8));
        } catch (Exception exception) {
            throw new IllegalStateException("Failed to sign JWT token.", exception);
        }
    }

    private long asLong(Object value) {
        if (value instanceof Number number) {
            return number.longValue();
        }
        return Long.parseLong(String.valueOf(value));
    }

    private Long asLongObject(Object value) {
        if (value == null) {
            return null;
        }
        return asLong(value);
    }

    private Integer asInteger(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof Number number) {
            return number.intValue();
        }
        return Integer.parseInt(String.valueOf(value));
    }

    private String asString(Object value) {
        return value == null ? null : String.valueOf(value);
    }
}

