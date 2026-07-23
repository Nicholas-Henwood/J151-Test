package com.vzap.trytons.model.notification;

import com.vzap.trytons.enums.NotificationType;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class Notification {
    private UUID notificationId;

    private NotificationType type;

    private String body;

    private LocalDateTime createdAt;

    private Boolean isRead;

    private String relatedEntityType;

    private UUID relatedEntityId;
    private UUID userId;
}