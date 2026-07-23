package com.vzap.trytons.dto.admin;

import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
@Builder
public class LogResponseDTO {
    private UUID logId;

    private String entityType;

    private UUID entityId;

    private String actionType;
    private String description;

    private LocalDateTime createdAt;

    private String ipAddress;

    private UUID notificationId;
    private UUID userId;
    private UUID transferId;
}
