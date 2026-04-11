package com.community.mvp.backend.infrastructure.persistence.content.entity;

import com.community.mvp.backend.domain.content.model.Course;
import com.community.mvp.backend.domain.content.model.CourseStatus;
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
@Table(name = "course")
public class CourseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 120)
    private String title;

    @Column(nullable = false, length = 1000)
    private String description;

    @Column(name = "cover_image", length = 255)
    private String coverImage;

    @Column(nullable = false, columnDefinition = "TINYINT")
    private int status;

    @Column(name = "moderation_status", nullable = false, columnDefinition = "TINYINT")
    private int moderationStatus;

    @Column(name = "moderation_reason", length = 255)
    private String moderationReason;

    @Column(name = "moderated_by")
    private Long moderatedBy;

    @Column(name = "moderated_at")
    private LocalDateTime moderatedAt;

    @Column(name = "created_by", nullable = false)
    private Long createdBy;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    public CourseEntity() {
    }

    public Course toDomain() {
        return new Course(
            id,
            title,
            description,
            coverImage,
            CourseStatus.fromCode(status),
            moderationStatus,
            moderationReason,
            moderatedBy,
            moderatedAt,
            createdBy,
            createdAt,
            updatedAt
        );
    }

    public void updateFrom(Course course) {
        this.title = course.title();
        this.description = course.description();
        this.coverImage = course.coverImage();
        this.status = course.status().getCode();
        this.moderationStatus = course.moderationStatus();
        this.moderationReason = course.moderationReason();
        this.moderatedBy = course.moderatedBy();
        this.moderatedAt = course.moderatedAt();
        this.createdBy = course.createdBy();
    }
}
