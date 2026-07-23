package za.ac.vzap.trytons.frontend.client.results;

import lombok.Getter;
import lombok.Setter;

import java.util.Map;
import java.util.UUID;

@Getter
@Setter
public class PlayerStatisticsCorrectionRequest {
    private UUID statId;
    private UUID correctionByAdminUserId;
    private String reason;
    private Map<String, Object> newValuesJson;
}
