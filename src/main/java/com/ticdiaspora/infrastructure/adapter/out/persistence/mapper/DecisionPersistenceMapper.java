package com.ticdiaspora.infrastructure.adapter.out.persistence.mapper;

import com.ticdiaspora.domain.model.Decision;
import com.ticdiaspora.infrastructure.adapter.out.persistence.entity.DecisionEntity;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface DecisionPersistenceMapper {
    Decision toDomain(DecisionEntity entity);

    DecisionEntity toEntity(Decision domain);

    List<Decision> toDomainList(List<DecisionEntity> entities);
}
