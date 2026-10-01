package com.ticdiaspora.audit.infrastructure.web;

import com.ticdiaspora.audit.infrastructure.persistence.AuditLogEntity;
import com.ticdiaspora.audit.infrastructure.persistence.AuditLogJpaRepository;
import com.ticdiaspora.shared.domain.enums.AuditAction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.UUID;

@RestController
@RequestMapping("/api/audit-logs")
@PreAuthorize("hasRole('PRESIDENT')")
public class AuditLogController {

    private final AuditLogJpaRepository auditLogs;

    public AuditLogController(AuditLogJpaRepository auditLogs) {
        this.auditLogs = auditLogs;
    }

    @GetMapping
    Page<AuditLogResponse> list(Pageable pageable) {
        return auditLogs.findAll(pageable).map(this::toResponse);
    }

    private AuditLogResponse toResponse(AuditLogEntity auditLog) {
        return new AuditLogResponse(auditLog.getId(), auditLog.getActorId(), auditLog.getAction(), auditLog.getEntityType(),
                auditLog.getEntityId(), auditLog.getOldValueJson(), auditLog.getNewValueJson(), auditLog.getReason(),
                auditLog.getCreatedAt());
    }

    public record AuditLogResponse(UUID id, UUID actorId, AuditAction action, String entityType, String entityId,
                                   String oldValueJson, String newValueJson, String reason, Instant createdAt) {
    }
}
