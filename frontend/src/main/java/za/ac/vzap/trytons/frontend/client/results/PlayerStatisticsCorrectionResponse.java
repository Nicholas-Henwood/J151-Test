package za.ac.vzap.trytons.frontend.client.results;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

@Getter
@Setter
public class PlayerStatisticsCorrectionResponse {
    private UUID correctionId;
    private UUID statId;
    private UUID correctionByAdminUserId;
    private String reason;
    private Map<String, Object> oldValuesJson;
    private Map<String, Object> newValuesJson;
    private LocalDateTime correctionTime;
}
