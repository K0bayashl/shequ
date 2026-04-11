package com.community.mvp.backend.infrastructure.persistence.moderation.entity;

import com.community.mvp.backend.domain.moderation.model.ActionAuditLog;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import org.hibernate.annotations.CreationTimestamp;

@Entity
@Table(name = "action_audit_log")
public class ActionAuditLogEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "actor_user_id", nullable = false)
    private Long actorUserId;

    @Column(name = "action_type", nullable = false, length = 64)
    private String actionType;

    @Column(name = "target_type", nullable = false, length = 32)
    private String targetType;

    @Column(name = "target_id", nullable = false)
    private Long targetId;

    @Column(name = "action_result", nullable = false, length = 32)
    private String actionResult;

    @Column(name = "detail_json", columnDefinition = "TEXT")
    private String detailJson;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public ActionAuditLogEntity() {
    }

    public ActionAuditLog toDomain() {
        return new ActionAuditLog(id, actorUserId, actionType, targetType, targetId, actionResult, detailJson, createdAt);
    }

    public void updateFrom(ActionAuditLog log) {
        this.actorUserId = log.actorUserId();
        this.actionType = log.actionType();
        this.targetType = log.targetType();
        this.targetId = log.targetId();
        this.actionResult = log.actionResult();
        this.detailJson = log.detailJson();
    }
}
