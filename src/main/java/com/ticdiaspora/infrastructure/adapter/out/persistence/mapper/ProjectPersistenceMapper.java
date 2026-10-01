package com.ticdiaspora.infrastructure.adapter.out.persistence.mapper;

import com.ticdiaspora.domain.model.Project;
import com.ticdiaspora.infrastructure.adapter.out.persistence.entity.ProjectEntity;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface ProjectPersistenceMapper {
    Project toDomain(ProjectEntity entity);

    ProjectEntity toEntity(Project domain);

    List<Project> toDomainList(List<ProjectEntity> entities);
}
