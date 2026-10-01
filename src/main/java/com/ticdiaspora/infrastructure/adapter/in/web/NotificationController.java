package com.ticdiaspora.infrastructure.adapter.in.web;

import com.ticdiaspora.application.usecase.*;
import com.ticdiaspora.application.port.out.*;
import com.ticdiaspora.infrastructure.adapter.in.web.dto.*;

import com.ticdiaspora.infrastructure.security.CurrentUserService;
import com.ticdiaspora.domain.model.Notification;
import com.ticdiaspora.application.port.out.NotificationRepositoryPort;
import com.ticdiaspora.domain.model.enums.NotificationChannel;
import com.ticdiaspora.domain.model.enums.NotificationStatus;
import com.ticdiaspora.domain.model.enums.NotificationType;
import com.ticdiaspora.domain.exception.NotFoundException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    private final NotificationRepositoryPort notifications;
    private final CurrentUserService currentUser;

    public NotificationController(NotificationRepositoryPort notifications, CurrentUserService currentUser) {
        this.notifications = notifications;
        this.currentUser = currentUser;
    }

    @GetMapping
    List<NotificationResponse> mine() {
        UUID memberId = currentUser.memberIdOrSystem();
        return notifications.findAllByRecipientIdOrderByCreatedAtDesc(memberId).stream().map(this::toResponse).toList();
    }

    @GetMapping("/all")
    @PreAuthorize("hasRole('PRESIDENT')")
    List<NotificationResponse> all() {
        return notifications.findAll().stream().map(this::toResponse).toList();
    }

    @PatchMapping("/{id}/read")
    NotificationResponse read(@PathVariable UUID id) {
        Notification notification = notifications.findById(id).orElseThrow(() -> new NotFoundException("Notification", id));
        notification.setStatus(NotificationStatus.READ);
        notification.setReadAt(Instant.now());
        return toResponse(notifications.save(notification));
    }

    private NotificationResponse toResponse(Notification notification) {
        return new NotificationResponse(notification.getId(), notification.getRecipientId(), notification.getType(),
                notification.getChannel(), notification.getTitle(), notification.getMessage(), notification.getStatus(),
                notification.getPayloadJson(), notification.getCreatedAt(), notification.getReadAt());
    }

    public record NotificationResponse(UUID id, UUID recipientId, NotificationType type, NotificationChannel channel,
                                       String title, String message, NotificationStatus status, String payloadJson,
                                       Instant createdAt, Instant readAt) {
    }
}
