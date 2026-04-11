package com.community.mvp.backend.interfaces.rest.content.dto;

public record DocsChapterItemResponse(
    String title,
    String href,
    boolean active
) {
}
