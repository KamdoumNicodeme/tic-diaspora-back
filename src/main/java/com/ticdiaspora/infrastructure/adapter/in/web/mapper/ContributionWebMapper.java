package com.ticdiaspora.infrastructure.adapter.in.web.mapper;

import com.ticdiaspora.domain.model.Contribution;
import com.ticdiaspora.infrastructure.adapter.in.web.dto.ContributionDtos;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ContributionWebMapper {
    @Mapping(target = "remainingAmount", expression = "java(Math.max(0, contribution.getExpectedAmount() - contribution.getPaidAmount()))")
    @Mapping(target = "memberFullName", ignore = true)
    ContributionDtos.ContributionResponse toResponse(Contribution contribution);
}
