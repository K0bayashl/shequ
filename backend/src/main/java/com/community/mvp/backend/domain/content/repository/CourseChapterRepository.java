package com.community.mvp.backend.domain.content.repository;

import com.community.mvp.backend.domain.content.model.CourseChapter;

import java.util.List;
import java.util.Optional;

public interface CourseChapterRepository {

    CourseChapter saveAndFlush(CourseChapter chapter);

    Optional<CourseChapter> findById(Long id);

    List<CourseChapter> findAllByCourseIdOrderBySortOrderAsc(Long courseId);

    long countByCourseId(Long courseId);
}
