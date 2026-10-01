package com.ticdiaspora.infrastructure.adapter.out.persistence.mapper;

import com.ticdiaspora.domain.model.User;
import com.ticdiaspora.infrastructure.adapter.out.persistence.entity.UserEntity;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface UserPersistenceMapper {
    User toDomain(UserEntity entity);

    UserEntity toEntity(User domain);

    List<User> toDomainList(List<UserEntity> entities);
}
