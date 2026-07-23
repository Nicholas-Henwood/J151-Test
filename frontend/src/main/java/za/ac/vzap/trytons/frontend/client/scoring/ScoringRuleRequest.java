package za.ac.vzap.trytons.frontend.client.scoring;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class ScoringRuleRequest {
    private UUID ruleId;
    private String eventType;
    private int pointsAwarded;
    private String season;
    private boolean active;
    @JsonProperty("isDeduction")
    private Boolean isDeduction;
    private String description;
}
