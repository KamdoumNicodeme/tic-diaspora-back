package com.ticdiaspora.audit.infrastructure.web;

import com.ticdiaspora.audit.infrastructure.persistence.AuditLogEntity;
import com.ticdiaspora.audit.infrastructure.persistence.AuditLogJpaRepository;
import com.ticdiaspora.auth.application.CurrentUserService;
import com.ticdiaspora.shared.domain.enums.AuditAction;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class AuditService {

    private final AuditLogJpaRepository auditLogs;
    private final CurrentUserService currentUser;

    public AuditService(AuditLogJpaRepository auditLogs, CurrentUserService currentUser) {
        this.auditLogs = auditLogs;
        this.currentUser = currentUser;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void record(AuditAction action, String entityType, Object entityId, String oldValueJson, String newValueJson, String reason) {
        AuditLogEntity auditLog = new AuditLogEntity();
        auditLog.setActorId(currentUser.memberIdOrSystem());
        auditLog.setAction(action);
        auditLog.setEntityType(entityType);
        auditLog.setEntityId(String.valueOf(entityId));
        auditLog.setOldValueJson(oldValueJson);
        auditLog.setNewValueJson(newValueJson);
        auditLog.setReason(reason);
        auditLogs.save(auditLog);
    }

    public void record(UUID actorId, AuditAction action, String entityType, Object entityId, String oldValueJson, String newValueJson, String reason) {
        AuditLogEntity auditLog = new AuditLogEntity();
        auditLog.setActorId(actorId);
        auditLog.setAction(action);
        auditLog.setEntityType(entityType);
        auditLog.setEntityId(String.valueOf(entityId));
        auditLog.setOldValueJson(oldValueJson);
        auditLog.setNewValueJson(newValueJson);
        auditLog.setReason(reason);
        auditLogs.save(auditLog);
    }
}
