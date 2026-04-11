package com.community.mvp.backend.infrastructure.persistence.content.entity;

import com.community.mvp.backend.domain.content.model.CourseChapter;
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
@Table(name = "course_chapter")
public class CourseChapterEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "course_id", nullable = false)
    private Long courseId;

    @Column(nullable = false, length = 120)
    private String title;

    @Column(nullable = false, columnDefinition = "LONGTEXT")
    private String content;

    @Column(name = "sort_order", nullable = false)
    private int sortOrder;

    @Column(name = "moderation_status", nullable = false, columnDefinition = "TINYINT")
    private int moderationStatus;

    @Column(name = "moderation_reason", length = 255)
    private String moderationReason;

    @Column(name = "moderated_by")
    private Long moderatedBy;

    @Column(name = "moderated_at")
    private LocalDateTime moderatedAt;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    public CourseChapterEntity() {
    }

    public Long getId() {
        return id;
    }

    public CourseChapter toDomain() {
        return new CourseChapter(
            id,
            courseId,
            title,
            content,
            sortOrder,
            moderationStatus,
            moderationReason,
            moderatedBy,
            moderatedAt,
            createdAt,
            updatedAt
        );
    }

    public void updateFrom(CourseChapter chapter) {
        this.courseId = chapter.courseId();
        this.title = chapter.title();
        this.content = chapter.content();
        this.sortOrder = chapter.sortOrder();
        this.moderationStatus = chapter.moderationStatus();
        this.moderationReason = chapter.moderationReason();
        this.moderatedBy = chapter.moderatedBy();
        this.moderatedAt = chapter.moderatedAt();
    }
}
