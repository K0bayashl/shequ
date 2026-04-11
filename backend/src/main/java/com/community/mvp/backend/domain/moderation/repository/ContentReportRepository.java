package com.community.mvp.backend.domain.moderation.repository;

import com.community.mvp.backend.domain.moderation.model.ContentReport;
import com.community.mvp.backend.domain.moderation.model.ReportStatus;
import java.util.List;
import java.util.Optional;

public interface ContentReportRepository {

    ContentReport saveAndFlush(ContentReport report);

    Optional<ContentReport> findById(Long id);

    List<ContentReport> findAllByOrderByCreatedAtDesc();

    List<ContentReport> findAllByStatusOrderByCreatedAtDesc(ReportStatus status);
}
