package com.ticdiaspora.application.port.out;

import com.ticdiaspora.domain.model.*;
import com.ticdiaspora.domain.model.enums.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.*;
import java.util.*;

public interface NotificationRepositoryPort {
    List<Notification> findAll();

    Optional<Notification> findById(UUID id);

    Notification save(Notification notification);

    List<Notification> findAllByRecipientIdOrderByCreatedAtDesc(UUID recipientId);

    boolean existsByRecipientIdAndTypeAndChannelAndPayloadJson(
            UUID recipientId,
            NotificationType type,
            NotificationChannel channel,
            String payloadJson
    );
}
