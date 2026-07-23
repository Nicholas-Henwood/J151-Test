package za.ac.vzap.trytons.frontend.client.notification;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
public class NotificationResponse {
    private UUID notificationId;
    private String type;
    private String body;
    private LocalDateTime createdAt;
    @JsonProperty("isRead")
    private boolean isRead;
    private String relatedEntityType;
    private UUID relatedEntityId;
}
