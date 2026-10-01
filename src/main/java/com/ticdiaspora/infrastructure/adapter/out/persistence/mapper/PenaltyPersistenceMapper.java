package com.ticdiaspora.infrastructure.adapter.out.persistence.mapper;

import com.ticdiaspora.domain.model.Penalty;
import com.ticdiaspora.infrastructure.adapter.out.persistence.entity.PenaltyEntity;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface PenaltyPersistenceMapper {
    Penalty toDomain(PenaltyEntity entity);

    PenaltyEntity toEntity(Penalty domain);

    List<Penalty> toDomainList(List<PenaltyEntity> entities);
}
