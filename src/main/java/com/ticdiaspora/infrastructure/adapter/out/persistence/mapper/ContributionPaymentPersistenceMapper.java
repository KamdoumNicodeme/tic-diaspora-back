package com.ticdiaspora.infrastructure.adapter.out.persistence.mapper;

import com.ticdiaspora.domain.model.ContributionPayment;
import com.ticdiaspora.infrastructure.adapter.out.persistence.entity.ContributionPaymentEntity;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface ContributionPaymentPersistenceMapper {
    ContributionPayment toDomain(ContributionPaymentEntity entity);

    ContributionPaymentEntity toEntity(ContributionPayment domain);

    List<ContributionPayment> toDomainList(List<ContributionPaymentEntity> entities);
}
