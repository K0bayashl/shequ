package com.community.mvp.backend.infrastructure.persistence.content.repository.jpa;

import com.community.mvp.backend.infrastructure.persistence.content.entity.CourseChapterEntity;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface JpaCourseChapterRepository extends JpaRepository<CourseChapterEntity, Long> {

    List<CourseChapterEntity> findAllByCourseIdOrderBySortOrderAscIdAsc(Long courseId);

    long countByCourseId(Long courseId);
}
