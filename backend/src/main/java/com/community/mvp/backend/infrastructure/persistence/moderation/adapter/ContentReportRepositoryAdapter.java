package com.community.mvp.backend.infrastructure.persistence.moderation.adapter;

import com.community.mvp.backend.domain.moderation.model.ContentReport;
import com.community.mvp.backend.domain.moderation.model.ReportStatus;
import com.community.mvp.backend.domain.moderation.repository.ContentReportRepository;
import com.community.mvp.backend.infrastructure.persistence.moderation.entity.ContentReportEntity;
import com.community.mvp.backend.infrastructure.persistence.moderation.repository.jpa.JpaContentReportRepository;
import java.util.List;
import java.util.Optional;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(prefix = "community-mvp.runtime", name = "database-enabled", havingValue = "true")
public class ContentReportRepositoryAdapter implements ContentReportRepository {

    private final JpaContentReportRepository contentReportRepository;

    public ContentReportRepositoryAdapter(JpaContentReportRepository contentReportRepository) {
        this.contentReportRepository = contentReportRepository;
    }

    @Override
    public ContentReport saveAndFlush(ContentReport report) {
        ContentReportEntity entity = report.id() == null
            ? new ContentReportEntity()
            : contentReportRepository.findById(report.id())
                .orElseThrow(() -> new IllegalStateException("Content report does not exist."));
        entity.updateFrom(report);
        return contentReportRepository.saveAndFlush(entity).toDomain();
    }

    @Override
    public Optional<ContentReport> findById(Long id) {
        return contentReportRepository.findById(id).map(ContentReportEntity::toDomain);
    }

    @Override
    public List<ContentReport> findAllByOrderByCreatedAtDesc() {
        return contentReportRepository.findAllByOrderByCreatedAtDesc().stream()
            .map(ContentReportEntity::toDomain)
            .toList();
    }

    @Override
    public List<ContentReport> findAllByStatusOrderByCreatedAtDesc(ReportStatus status) {
        return contentReportRepository.findAllByStatusOrderByCreatedAtDesc(status.getCode()).stream()
            .map(ContentReportEntity::toDomain)
            .toList();
    }
}
