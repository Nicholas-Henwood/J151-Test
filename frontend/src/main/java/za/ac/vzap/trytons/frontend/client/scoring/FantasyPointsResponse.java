package za.ac.vzap.trytons.frontend.client.scoring;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
public class FantasyPointsResponse {
    private UUID pointsId;
    private UUID statId;
    private int totalPoints;
    private int calculationVersion;
    @JsonProperty("isFinal")
    private boolean isFinal;
    private LocalDateTime calculatedAt;
}
