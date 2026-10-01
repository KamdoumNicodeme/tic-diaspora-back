package com.ticdiaspora.infrastructure.adapter.out.persistence.mapper;

import com.ticdiaspora.domain.model.AuditLog;
import com.ticdiaspora.infrastructure.adapter.out.persistence.entity.AuditLogEntity;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface AuditLogPersistenceMapper {
    AuditLog toDomain(AuditLogEntity entity);

    AuditLogEntity toEntity(AuditLog domain);

    List<AuditLog> toDomainList(List<AuditLogEntity> entities);
}
