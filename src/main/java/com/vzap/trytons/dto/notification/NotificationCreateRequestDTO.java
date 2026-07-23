package com.vzap.trytons.dto.notification;

import com.vzap.trytons.enums.NotificationType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NotificationCreateRequestDTO {
    private UUID userId;

    private NotificationType type;

    private String body;
    private String relatedEntityType;

    private UUID relatedEntityId;
}
