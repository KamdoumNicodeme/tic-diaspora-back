package com.ticdiaspora.infrastructure.adapter.out.persistence.mapper;

import com.ticdiaspora.domain.model.Member;
import com.ticdiaspora.infrastructure.adapter.out.persistence.entity.MemberEntity;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface MemberPersistenceMapper {
    Member toDomain(MemberEntity entity);

    MemberEntity toEntity(Member domain);

    List<Member> toDomainList(List<MemberEntity> entities);
}
