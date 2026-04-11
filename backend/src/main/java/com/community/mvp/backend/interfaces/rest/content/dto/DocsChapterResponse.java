package com.community.mvp.backend.interfaces.rest.content.dto;

import java.util.List;

public record DocsChapterResponse(
    String title,
    String iconKey,
    List<DocsChapterItemResponse> items
) {
}
