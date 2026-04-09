package com.community.mvp.backend.domain;

import java.time.Instant;

public record ScaffoldEcho(String message, Instant echoedAt) {
}
