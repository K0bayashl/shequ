package com.community.mvp.backend.infrastructure.persistence.moderation.repository.jpa;

import com.community.mvp.backend.infrastructure.persistence.moderation.entity.ContentReportEntity;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface JpaContentReportRepository extends JpaRepository<ContentReportEntity, Long> {

    List<ContentReportEntity> findAllByOrderByCreatedAtDesc();

    List<ContentReportEntity> findAllByStatusOrderByCreatedAtDesc(int status);
}
