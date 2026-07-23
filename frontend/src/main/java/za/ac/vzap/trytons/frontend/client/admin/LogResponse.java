package za.ac.vzap.trytons.frontend.client.admin;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class LogResponse {
    private String logId;
    private String entityType;
    private String entityId;
    private String actionType;
    private String description;
    private LocalDateTime createdAt;
    private String ipAddress;
    private String notificationId;
    private String userId;
    private String transferId;
}
