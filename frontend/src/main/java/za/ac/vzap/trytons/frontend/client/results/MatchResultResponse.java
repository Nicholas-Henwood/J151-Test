package za.ac.vzap.trytons.frontend.client.results;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
public class MatchResultResponse {
    private UUID resultId;
    private UUID fixtureId;
    private UUID teamAId;
    private UUID teamBId;
    private int simulationRunNumber;
    private int teamAScore;
    private int teamBScore;
    private String winnerSide;
    @JsonProperty("isDraw")
    private boolean isDraw;
    private boolean approved;
    @JsonProperty("isCurrent")
    private boolean isCurrent;
    private LocalDateTime resultDate;
    private UUID approvedByAdminUserId;
}
