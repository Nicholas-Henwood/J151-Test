package za.ac.vzap.trytons.frontend.client.fantasyteam;

import lombok.Getter;
import lombok.Setter;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
public class ViewOpponentTeamResponse {
    private UUID teamId;
    private String teamName;
    private int totalPoints;
    private int weeklyPoints;
    private List<FantasyTeamPlayerSelectionResponse> players;
}
