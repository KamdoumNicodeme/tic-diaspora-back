package com.ticdiaspora.infrastructure.adapter.out.persistence.mapper;

import com.ticdiaspora.domain.model.BeneficiaryConfirmation;
import com.ticdiaspora.infrastructure.adapter.out.persistence.entity.BeneficiaryConfirmationEntity;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface BeneficiaryConfirmationPersistenceMapper {
    BeneficiaryConfirmation toDomain(BeneficiaryConfirmationEntity entity);

    BeneficiaryConfirmationEntity toEntity(BeneficiaryConfirmation domain);

    List<BeneficiaryConfirmation> toDomainList(List<BeneficiaryConfirmationEntity> entities);
}
