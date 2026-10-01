package com.ticdiaspora.infrastructure.adapter.out.persistence;

import com.ticdiaspora.application.port.out.NotificationRepositoryPort;
import com.ticdiaspora.domain.model.*;
import com.ticdiaspora.domain.model.enums.*;
import com.ticdiaspora.infrastructure.adapter.out.persistence.mapper.NotificationPersistenceMapper;
import com.ticdiaspora.infrastructure.adapter.out.persistence.repository.NotificationJpaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.time.*;
import java.util.*;

@Repository
public class NotificationPersistenceAdapter implements NotificationRepositoryPort {

    private final NotificationJpaRepository repository;
    private final NotificationPersistenceMapper mapper;

    public NotificationPersistenceAdapter(NotificationJpaRepository repository, NotificationPersistenceMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    @Override
    public List<Notification> findAll() {
        return mapper.toDomainList(repository.findAll());
    }
    @Override
    public Optional<Notification> findById(UUID id) {
        return repository.findById(id).map(mapper::toDomain);
    }
    @Override
    public Notification save(Notification notification) {
        return mapper.toDomain(repository.save(mapper.toEntity(notification)));
    }
    @Override
    public List<Notification> findAllByRecipientIdOrderByCreatedAtDesc(UUID recipientId) {
        return mapper.toDomainList(repository.findAllByRecipientIdOrderByCreatedAtDesc(recipientId));
    }

    @Override
    public boolean existsByRecipientIdAndTypeAndChannelAndPayloadJson(
            UUID recipientId,
            NotificationType type,
            NotificationChannel channel,
            String payloadJson
    ) {
        return repository.existsByRecipientIdAndTypeAndChannelAndPayloadJson(recipientId, type, channel, payloadJson);
    }
}
