package com.vzap.trytons.dto.notification;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.vzap.trytons.enums.NotificationType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NotificationResponseDTO {
    private UUID notificationId;

    private NotificationType type;

    private String body;

    private LocalDateTime createdAt;

    @JsonProperty("isRead")
    private boolean isRead;

    private String relatedEntityType;

    private UUID relatedEntityId;
}
