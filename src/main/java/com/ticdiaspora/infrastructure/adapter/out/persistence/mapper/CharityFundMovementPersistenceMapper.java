package com.ticdiaspora.infrastructure.adapter.out.persistence.mapper;

import com.ticdiaspora.domain.model.CharityFundMovement;
import com.ticdiaspora.infrastructure.adapter.out.persistence.entity.CharityFundMovementEntity;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface CharityFundMovementPersistenceMapper {
    CharityFundMovement toDomain(CharityFundMovementEntity entity);

    CharityFundMovementEntity toEntity(CharityFundMovement domain);

    List<CharityFundMovement> toDomainList(List<CharityFundMovementEntity> entities);
}
