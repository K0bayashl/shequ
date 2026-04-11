package com.community.mvp.backend.infrastructure.persistence.moderation.entity;

import com.community.mvp.backend.domain.moderation.model.ContentReport;
import com.community.mvp.backend.domain.moderation.model.ReportContentType;
import com.community.mvp.backend.domain.moderation.model.ReportStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

@Entity
@Table(name = "content_report")
public class ContentReportEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "content_type", nullable = false, length = 32)
    private String contentType;

    @Column(name = "content_id", nullable = false)
    private Long contentId;

    @Column(name = "reporter_user_id", nullable = false)
    private Long reporterUserId;

    @Column(name = "reason_code", nullable = false, length = 32)
    private String reasonCode;

    @Column(name = "reason_detail", length = 500)
    private String reasonDetail;

    @Column(nullable = false, columnDefinition = "TINYINT")
    private int status;

    @Column(name = "handled_by")
    private Long handledBy;

    @Column(name = "handled_at")
    private LocalDateTime handledAt;

    @Column(name = "handle_note", length = 500)
    private String handleNote;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    public ContentReportEntity() {
    }

    public ContentReport toDomain() {
        return new ContentReport(
            id,
            ReportContentType.fromCode(contentType),
            contentId,
            reporterUserId,
            reasonCode,
            reasonDetail,
            ReportStatus.fromCode(status),
            handledBy,
            handledAt,
            handleNote,
            createdAt,
            updatedAt
        );
    }

    public void updateFrom(ContentReport report) {
        this.contentType = report.contentType().getCode();
        this.contentId = report.contentId();
        this.reporterUserId = report.reporterUserId();
        this.reasonCode = report.reasonCode();
        this.reasonDetail = report.reasonDetail();
        this.status = report.status().getCode();
        this.handledBy = report.handledBy();
        this.handledAt = report.handledAt();
        this.handleNote = report.handleNote();
    }
}
