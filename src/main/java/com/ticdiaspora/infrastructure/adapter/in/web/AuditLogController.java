package com.ticdiaspora.infrastructure.adapter.in.web;

import com.ticdiaspora.application.usecase.*;
import com.ticdiaspora.application.port.out.*;
import com.ticdiaspora.infrastructure.adapter.in.web.dto.*;

import com.ticdiaspora.domain.model.AuditLog;
import com.ticdiaspora.application.port.out.AuditLogRepositoryPort;
import com.ticdiaspora.domain.model.enums.AuditAction;
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

    private final AuditLogRepositoryPort auditLogs;

    public AuditLogController(AuditLogRepositoryPort auditLogs) {
        this.auditLogs = auditLogs;
    }

    @GetMapping
    Page<AuditLogResponse> list(Pageable pageable) {
        return auditLogs.findAll(pageable).map(this::toResponse);
    }

    private AuditLogResponse toResponse(AuditLog auditLog) {
        return new AuditLogResponse(auditLog.getId(), auditLog.getActorId(), auditLog.getAction(), auditLog.getEntityType(),
                auditLog.getEntityId(), auditLog.getOldValueJson(), auditLog.getNewValueJson(), auditLog.getReason(),
                auditLog.getCreatedAt());
    }

    public record AuditLogResponse(UUID id, UUID actorId, AuditAction action, String entityType, String entityId,
                                   String oldValueJson, String newValueJson, String reason, Instant createdAt) {
    }
}
