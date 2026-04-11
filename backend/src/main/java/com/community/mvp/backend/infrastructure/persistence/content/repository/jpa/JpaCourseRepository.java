package com.community.mvp.backend.infrastructure.persistence.content.repository.jpa;

import com.community.mvp.backend.infrastructure.persistence.content.entity.CourseEntity;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface JpaCourseRepository extends JpaRepository<CourseEntity, Long> {

    List<CourseEntity> findAllByStatusOrderByUpdatedAtDesc(int status);
}
