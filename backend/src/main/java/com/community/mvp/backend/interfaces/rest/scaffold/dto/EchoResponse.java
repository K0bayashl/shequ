package com.community.mvp.backend.interfaces.rest.scaffold.dto;

import java.time.Instant;

public record EchoResponse(String message, Instant echoedAt) {
}

