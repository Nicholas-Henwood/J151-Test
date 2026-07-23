package za.ac.vzap.trytons.frontend.client.fantasyteam;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.util.List;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class FantasyTeamRequest {
    private String teamName;
    private List<FantasyTeamPlayerSelectionRequest> selectedPlayers;
}
