package com.ticdiaspora.domain.model;

import com.ticdiaspora.domain.model.enums.NotificationChannel;
import com.ticdiaspora.domain.model.enums.NotificationStatus;
import com.ticdiaspora.domain.model.enums.NotificationType;
import java.time.Instant;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class Notification {

    private UUID id;
    private UUID recipientId;
    private NotificationType type;
    @Builder.Default
    private NotificationChannel channel = NotificationChannel.IN_APP;
    private String title;
    private String message;
    @Builder.Default
    private NotificationStatus status = NotificationStatus.UNREAD;
    private String payloadJson;
    private Instant createdAt;
    private Instant readAt;
}
