package com.ticdiaspora.infrastructure.adapter.out.persistence.mapper;

import com.ticdiaspora.domain.model.TontineCycle;
import com.ticdiaspora.infrastructure.adapter.out.persistence.entity.TontineCycleEntity;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface TontineCyclePersistenceMapper {
    TontineCycle toDomain(TontineCycleEntity entity);

    TontineCycleEntity toEntity(TontineCycle domain);

    List<TontineCycle> toDomainList(List<TontineCycleEntity> entities);
}
