package com.community.mvp.backend.interfaces.rest.dto;

public record SecurePingResponse(boolean authenticated, String principal) {
}
