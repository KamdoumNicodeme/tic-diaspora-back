package com.ticdiaspora.infrastructure.adapter.out.persistence;

import com.ticdiaspora.application.port.out.MeetingChairpersonHistoryRepositoryPort;
import com.ticdiaspora.domain.model.*;
import com.ticdiaspora.domain.model.enums.*;
import com.ticdiaspora.infrastructure.adapter.out.persistence.mapper.MeetingChairpersonHistoryPersistenceMapper;
import com.ticdiaspora.infrastructure.adapter.out.persistence.repository.MeetingChairpersonHistoryJpaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.time.*;
import java.util.*;

@Repository
public class MeetingChairpersonHistoryPersistenceAdapter implements MeetingChairpersonHistoryRepositoryPort {

    private final MeetingChairpersonHistoryJpaRepository repository;
    private final MeetingChairpersonHistoryPersistenceMapper mapper;

    public MeetingChairpersonHistoryPersistenceAdapter(MeetingChairpersonHistoryJpaRepository repository, MeetingChairpersonHistoryPersistenceMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    @Override
    public MeetingChairpersonHistory save(MeetingChairpersonHistory meetingChairpersonHistory) {
        return mapper.toDomain(repository.save(mapper.toEntity(meetingChairpersonHistory)));
    }
    @Override
    public List<MeetingChairpersonHistory> findAllByMemberIdOrderByAssignedAtDesc(UUID memberId) {
        return mapper.toDomainList(repository.findAllByMemberIdOrderByAssignedAtDesc(memberId));
    }
}
