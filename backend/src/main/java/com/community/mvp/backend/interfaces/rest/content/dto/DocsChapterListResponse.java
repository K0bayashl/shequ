package com.community.mvp.backend.interfaces.rest.content.dto;

import java.util.List;

public record DocsChapterListResponse(
    List<DocsChapterResponse> chapters
) {
}
