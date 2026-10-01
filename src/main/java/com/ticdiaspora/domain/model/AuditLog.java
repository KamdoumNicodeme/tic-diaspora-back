package com.ticdiaspora.domain.model;

import com.ticdiaspora.domain.model.enums.AuditAction;
import java.time.Instant;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class AuditLog {

    private UUID id;
    private UUID actorId;
    private AuditAction action;
    private String entityType;
    private String entityId;
    private String oldValueJson;
    private String newValueJson;
    private String reason;
    private Instant createdAt;
}
