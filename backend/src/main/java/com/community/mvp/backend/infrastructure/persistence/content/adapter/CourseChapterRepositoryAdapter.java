package com.community.mvp.backend.infrastructure.persistence.content.adapter;

import com.community.mvp.backend.domain.content.model.CourseChapter;
import com.community.mvp.backend.domain.content.repository.CourseChapterRepository;
import com.community.mvp.backend.infrastructure.persistence.content.entity.CourseChapterEntity;
import com.community.mvp.backend.infrastructure.persistence.content.repository.jpa.JpaCourseChapterRepository;
import java.util.List;
import java.util.Optional;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(prefix = "community-mvp.runtime", name = "database-enabled", havingValue = "true")
public class CourseChapterRepositoryAdapter implements CourseChapterRepository {

    private final JpaCourseChapterRepository chapterRepository;

    public CourseChapterRepositoryAdapter(JpaCourseChapterRepository chapterRepository) {
        this.chapterRepository = chapterRepository;
    }

    @Override
    public CourseChapter saveAndFlush(CourseChapter chapter) {
        CourseChapterEntity entity = chapter.id() == null
            ? new CourseChapterEntity()
            : chapterRepository.findById(chapter.id())
                .orElseThrow(() -> new IllegalStateException("Course chapter does not exist."));
        entity.updateFrom(chapter);
        return chapterRepository.saveAndFlush(entity).toDomain();
    }

    @Override
    public Optional<CourseChapter> findById(Long id) {
        return chapterRepository.findById(id).map(CourseChapterEntity::toDomain);
    }

    @Override
    public List<CourseChapter> findAllByCourseIdOrderBySortOrderAsc(Long courseId) {
        return chapterRepository.findAllByCourseIdOrderBySortOrderAscIdAsc(courseId).stream()
            .map(CourseChapterEntity::toDomain)
            .toList();
    }

    @Override
    public long countByCourseId(Long courseId) {
        return chapterRepository.countByCourseId(courseId);
    }
}
