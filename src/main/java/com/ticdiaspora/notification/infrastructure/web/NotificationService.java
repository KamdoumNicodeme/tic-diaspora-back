package com.ticdiaspora.notification.infrastructure.web;

import com.ticdiaspora.notification.infrastructure.persistence.NotificationEntity;
import com.ticdiaspora.notification.infrastructure.persistence.NotificationJpaRepository;
import com.ticdiaspora.shared.domain.enums.NotificationChannel;
import com.ticdiaspora.shared.domain.enums.NotificationType;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class NotificationService {

    private final NotificationJpaRepository notifications;

    public NotificationService(NotificationJpaRepository notifications) {
        this.notifications = notifications;
    }

    public void notifyInApp(UUID recipientId, NotificationType type, String title, String message, String payloadJson) {
        NotificationEntity notification = new NotificationEntity();
        notification.setRecipientId(recipientId);
        notification.setType(type);
        notification.setChannel(NotificationChannel.IN_APP);
        notification.setTitle(title);
        notification.setMessage(message);
        notification.setPayloadJson(payloadJson);
        notifications.save(notification);
    }
}
