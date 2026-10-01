package com.ticdiaspora.application.usecase;
import com.ticdiaspora.application.port.in.*;
import com.ticdiaspora.application.port.out.*;

import com.ticdiaspora.domain.model.Notification;
import com.ticdiaspora.application.port.out.EmailSenderPort;
import com.ticdiaspora.application.port.out.NotificationRepositoryPort;
import com.ticdiaspora.domain.model.enums.NotificationChannel;
import com.ticdiaspora.domain.model.enums.NotificationStatus;
import com.ticdiaspora.domain.model.enums.NotificationType;
import com.ticdiaspora.application.annotation.UseCase;

import java.util.UUID;

@UseCase
public class NotificationService {

    private final NotificationRepositoryPort notifications;
    private final EmailSenderPort emailSender;

    public NotificationService(NotificationRepositoryPort notifications, EmailSenderPort emailSender) {
        this.notifications = notifications;
        this.emailSender = emailSender;
    }

    public void notifyInApp(UUID recipientId, NotificationType type, String title, String message, String payloadJson) {
        saveNotification(recipientId, NotificationChannel.IN_APP, type, title, message, payloadJson);
    }

    public void notifyEmail(UUID recipientId, String to, NotificationType type, String title, String message, String payloadJson) {
        saveNotification(recipientId, NotificationChannel.EMAIL, type, title, message, payloadJson);
        emailSender.send(to, title, message);
    }

    public boolean alreadyNotified(UUID recipientId, NotificationType type, NotificationChannel channel, String payloadJson) {
        return notifications.existsByRecipientIdAndTypeAndChannelAndPayloadJson(recipientId, type, channel, payloadJson);
    }

    private void saveNotification(UUID recipientId, NotificationChannel channel, NotificationType type, String title, String message, String payloadJson) {
        Notification notification = new Notification();
        notification.setRecipientId(recipientId);
        notification.setType(type);
        notification.setChannel(channel);
        notification.setStatus(NotificationStatus.UNREAD);
        notification.setTitle(title);
        notification.setMessage(message);
        notification.setPayloadJson(payloadJson);
        notifications.save(notification);
    }
}
