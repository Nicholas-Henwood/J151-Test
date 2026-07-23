package za.ac.vzap.trytons.frontend.client.results;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
public class MatchTeamScoreResponse {
    private UUID teamScoreId;
    private UUID resultId;
    private UUID teamId;
    private String teamSide;
    private int playerPoints;
    private int captainBonus;
    private int transferPenalty;
    private int totalScore;
    private LocalDateTime calculatedAt;
}
