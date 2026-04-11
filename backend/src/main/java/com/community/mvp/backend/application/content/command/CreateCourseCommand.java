package com.community.mvp.backend.application.content.command;

import java.util.List;

public record CreateCourseCommand(
    String title,
    String description,
    String coverImage,
    int status,
    List<CreateCourseChapterCommand> chapters,
    Long creatorUserId
) {
}
