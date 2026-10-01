package com.ticdiaspora.infrastructure.adapter.out.persistence.mapper;

import com.ticdiaspora.domain.model.Contribution;
import com.ticdiaspora.infrastructure.adapter.out.persistence.entity.ContributionEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface ContributionPersistenceMapper {
    @Mapping(target = "lastPaymentMethod", ignore = true)
    @Mapping(target = "lastTransactionReference", ignore = true)
    @Mapping(target = "lastProofUrl", ignore = true)
    @Mapping(target = "lastPaymentAt", ignore = true)
    Contribution toDomain(ContributionEntity entity);

    ContributionEntity toEntity(Contribution domain);

    List<Contribution> toDomainList(List<ContributionEntity> entities);
}
