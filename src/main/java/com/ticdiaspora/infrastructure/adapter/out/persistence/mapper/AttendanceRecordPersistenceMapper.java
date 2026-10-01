package com.ticdiaspora.infrastructure.adapter.out.persistence.mapper;

import com.ticdiaspora.domain.model.AttendanceRecord;
import com.ticdiaspora.infrastructure.adapter.out.persistence.entity.AttendanceRecordEntity;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface AttendanceRecordPersistenceMapper {
    AttendanceRecord toDomain(AttendanceRecordEntity entity);

    AttendanceRecordEntity toEntity(AttendanceRecord domain);

    List<AttendanceRecord> toDomainList(List<AttendanceRecordEntity> entities);
}
