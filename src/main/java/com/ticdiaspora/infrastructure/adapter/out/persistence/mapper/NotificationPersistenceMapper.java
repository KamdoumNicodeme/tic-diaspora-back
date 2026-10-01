package com.ticdiaspora.infrastructure.adapter.out.persistence.mapper;

import com.ticdiaspora.domain.model.Notification;
import com.ticdiaspora.infrastructure.adapter.out.persistence.entity.NotificationEntity;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface NotificationPersistenceMapper {
    Notification toDomain(NotificationEntity entity);

    NotificationEntity toEntity(Notification domain);

    List<Notification> toDomainList(List<NotificationEntity> entities);
}
