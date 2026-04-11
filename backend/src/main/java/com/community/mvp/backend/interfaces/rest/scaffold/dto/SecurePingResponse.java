package com.community.mvp.backend.interfaces.rest.scaffold.dto;

public record SecurePingResponse(boolean authenticated, String principal) {
}

