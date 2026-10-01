package com.ticdiaspora.infrastructure.adapter.out.persistence.repository;

import com.ticdiaspora.infrastructure.adapter.out.persistence.entity.*;

import com.ticdiaspora.domain.model.enums.NotificationChannel;
import com.ticdiaspora.domain.model.enums.NotificationType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface NotificationJpaRepository extends JpaRepository<NotificationEntity, UUID> {
    List<NotificationEntity> findAllByRecipientIdOrderByCreatedAtDesc(UUID recipientId);

    boolean existsByRecipientIdAndTypeAndChannelAndPayloadJson(
            UUID recipientId,
            NotificationType type,
            NotificationChannel channel,
            String payloadJson
    );
}
