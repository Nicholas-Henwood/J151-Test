package za.ac.vzap.trytons.frontend.client.results;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
public class PlayerStatisticsResponse {
    private UUID statId;
    private UUID resultId;
    private UUID teamId;
    private UUID playerId;
    private int tries;
    private int assists;
    private int tackles;
    private int missedTackles;
    private int conversions;
    private int penalties;
    private int metersGained;
    private int yellowCards;
    private int redCards;
    private LocalDateTime statisticDate;
}
