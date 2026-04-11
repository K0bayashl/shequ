package com.community.mvp.backend.infrastructure.persistence.moderation.adapter;

import com.community.mvp.backend.domain.moderation.model.ActionAuditLog;
import com.community.mvp.backend.domain.moderation.repository.ActionAuditLogRepository;
import com.community.mvp.backend.infrastructure.persistence.moderation.entity.ActionAuditLogEntity;
import com.community.mvp.backend.infrastructure.persistence.moderation.repository.jpa.JpaActionAuditLogRepository;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(prefix = "community-mvp.runtime", name = "database-enabled", havingValue = "true")
public class ActionAuditLogRepositoryAdapter implements ActionAuditLogRepository {

    private final JpaActionAuditLogRepository actionAuditLogRepository;

    public ActionAuditLogRepositoryAdapter(JpaActionAuditLogRepository actionAuditLogRepository) {
        this.actionAuditLogRepository = actionAuditLogRepository;
    }

    @Override
    public ActionAuditLog saveAndFlush(ActionAuditLog auditLog) {
        ActionAuditLogEntity entity = new ActionAuditLogEntity();
        entity.updateFrom(auditLog);
        return actionAuditLogRepository.saveAndFlush(entity).toDomain();
    }
}
