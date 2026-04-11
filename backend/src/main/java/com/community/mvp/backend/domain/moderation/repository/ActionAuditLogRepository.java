package com.community.mvp.backend.domain.moderation.repository;

import com.community.mvp.backend.domain.moderation.model.ActionAuditLog;

public interface ActionAuditLogRepository {

    ActionAuditLog saveAndFlush(ActionAuditLog auditLog);
}
