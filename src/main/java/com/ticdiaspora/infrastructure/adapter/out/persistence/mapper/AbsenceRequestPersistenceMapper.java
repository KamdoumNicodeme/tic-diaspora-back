package com.ticdiaspora.infrastructure.adapter.out.persistence.mapper;

import com.ticdiaspora.domain.model.AbsenceRequest;
import com.ticdiaspora.infrastructure.adapter.out.persistence.entity.AbsenceRequestEntity;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface AbsenceRequestPersistenceMapper {
    AbsenceRequest toDomain(AbsenceRequestEntity entity);

    AbsenceRequestEntity toEntity(AbsenceRequest domain);

    List<AbsenceRequest> toDomainList(List<AbsenceRequestEntity> entities);
}
