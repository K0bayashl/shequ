package com.community.mvp.backend.domain.content.repository;

import com.community.mvp.backend.domain.content.model.Course;
import com.community.mvp.backend.domain.content.model.CourseStatus;

import java.util.List;
import java.util.Optional;

public interface CourseRepository {

    Course saveAndFlush(Course course);

    Optional<Course> findById(Long id);

    List<Course> findAllByStatusOrderByUpdatedAtDesc(CourseStatus status);
}
