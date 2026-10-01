package com.ticdiaspora.application.usecase;
import com.ticdiaspora.application.port.in.*;
import com.ticdiaspora.application.port.out.*;

import com.ticdiaspora.domain.model.AuditLog;
import com.ticdiaspora.application.port.out.AuditLogRepositoryPort;
import com.ticdiaspora.application.port.out.CurrentUserPort;
import com.ticdiaspora.domain.model.enums.AuditAction;
import com.ticdiaspora.application.annotation.UseCase;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@UseCase
public class AuditService {

    private final AuditLogRepositoryPort auditLogs;
    private final CurrentUserPort currentUser;

    public AuditService(AuditLogRepositoryPort auditLogs, CurrentUserPort currentUser) {
        this.auditLogs = auditLogs;
        this.currentUser = currentUser;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void record(AuditAction action, String entityType, Object entityId, String oldValueJson, String newValueJson, String reason) {
        AuditLog auditLog = new AuditLog();
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
        AuditLog auditLog = new AuditLog();
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
