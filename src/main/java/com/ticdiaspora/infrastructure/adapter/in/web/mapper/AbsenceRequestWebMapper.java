package com.ticdiaspora.infrastructure.adapter.in.web.mapper;

import com.ticdiaspora.domain.model.AbsenceRequest;
import com.ticdiaspora.infrastructure.adapter.in.web.dto.AbsenceRequestDtos;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface AbsenceRequestWebMapper {
    @Mapping(target = "memberFullName", ignore = true)
    @Mapping(target = "meetingTitle", ignore = true)
    @Mapping(target = "meetingDate", ignore = true)
    @Mapping(target = "plannedStartTime", ignore = true)
    AbsenceRequestDtos.AbsenceRequestResponse toResponse(AbsenceRequest request);
}
