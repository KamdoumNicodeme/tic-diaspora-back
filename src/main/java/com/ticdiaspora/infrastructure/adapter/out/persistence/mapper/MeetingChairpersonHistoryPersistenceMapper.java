package com.ticdiaspora.infrastructure.adapter.out.persistence.mapper;

import com.ticdiaspora.domain.model.MeetingChairpersonHistory;
import com.ticdiaspora.infrastructure.adapter.out.persistence.entity.MeetingChairpersonHistoryEntity;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface MeetingChairpersonHistoryPersistenceMapper {
    MeetingChairpersonHistory toDomain(MeetingChairpersonHistoryEntity entity);

    MeetingChairpersonHistoryEntity toEntity(MeetingChairpersonHistory domain);

    List<MeetingChairpersonHistory> toDomainList(List<MeetingChairpersonHistoryEntity> entities);
}
