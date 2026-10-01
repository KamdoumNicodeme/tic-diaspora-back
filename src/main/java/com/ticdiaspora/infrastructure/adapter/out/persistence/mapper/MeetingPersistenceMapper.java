package com.ticdiaspora.infrastructure.adapter.out.persistence.mapper;

import com.ticdiaspora.domain.model.Meeting;
import com.ticdiaspora.infrastructure.adapter.out.persistence.entity.MeetingEntity;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface MeetingPersistenceMapper {
    Meeting toDomain(MeetingEntity entity);

    MeetingEntity toEntity(Meeting domain);

    List<Meeting> toDomainList(List<MeetingEntity> entities);
}
