package com.community.mvp.backend.infrastructure.persistence.content.adapter;

import com.community.mvp.backend.domain.content.model.Course;
import com.community.mvp.backend.domain.content.model.CourseStatus;
import com.community.mvp.backend.domain.content.repository.CourseRepository;
import com.community.mvp.backend.infrastructure.persistence.content.entity.CourseEntity;
import com.community.mvp.backend.infrastructure.persistence.content.repository.jpa.JpaCourseRepository;
import java.util.List;
import java.util.Optional;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(prefix = "community-mvp.runtime", name = "database-enabled", havingValue = "true")
public class CourseRepositoryAdapter implements CourseRepository {

    private final JpaCourseRepository courseRepository;

    public CourseRepositoryAdapter(JpaCourseRepository courseRepository) {
        this.courseRepository = courseRepository;
    }

    @Override
    public Course saveAndFlush(Course course) {
        CourseEntity entity = course.id() == null
            ? new CourseEntity()
            : courseRepository.findById(course.id())
                .orElseThrow(() -> new IllegalStateException("Course does not exist."));
        entity.updateFrom(course);
        return courseRepository.saveAndFlush(entity).toDomain();
    }

    @Override
    public Optional<Course> findById(Long id) {
        return courseRepository.findById(id).map(CourseEntity::toDomain);
    }

    @Override
    public List<Course> findAllByStatusOrderByUpdatedAtDesc(CourseStatus status) {
        return courseRepository.findAllByStatusOrderByUpdatedAtDesc(status.getCode()).stream()
            .map(CourseEntity::toDomain)
            .toList();
    }
}
