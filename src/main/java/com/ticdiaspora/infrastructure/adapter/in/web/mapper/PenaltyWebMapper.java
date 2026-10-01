package com.ticdiaspora.infrastructure.adapter.in.web.mapper;

import com.ticdiaspora.domain.model.Penalty;
import com.ticdiaspora.infrastructure.adapter.in.web.dto.PenaltyDtos;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface PenaltyWebMapper {
    PenaltyDtos.PenaltyResponse toResponse(Penalty penalty);
}
