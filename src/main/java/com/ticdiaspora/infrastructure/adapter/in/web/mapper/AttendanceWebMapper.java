package com.ticdiaspora.infrastructure.adapter.in.web.mapper;

import com.ticdiaspora.domain.model.AttendanceRecord;
import com.ticdiaspora.infrastructure.adapter.in.web.dto.AttendanceDtos;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface AttendanceWebMapper {
    AttendanceDtos.AttendanceResponse toResponse(AttendanceRecord record);
}
