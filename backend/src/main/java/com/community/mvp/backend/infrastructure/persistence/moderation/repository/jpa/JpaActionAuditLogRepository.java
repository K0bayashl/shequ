package com.community.mvp.backend.infrastructure.persistence.moderation.repository.jpa;

import com.community.mvp.backend.infrastructure.persistence.moderation.entity.ActionAuditLogEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface JpaActionAuditLogRepository extends JpaRepository<ActionAuditLogEntity, Long> {
}
