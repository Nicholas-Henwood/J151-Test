package za.ac.vzap.trytons.frontend.client.scoring;

import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class FantasyPointBreakdownResponse {
    private UUID breakdownId;
    private UUID pointsId;
    private String category;
    private int points;
    private String description;
}
