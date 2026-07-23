package za.ac.vzap.trytons.frontend.client.league;

import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class JoinLeagueResponse {
    private UUID leagueId;
    private String leagueName;
    private String message;
    private UUID membershipId;
}
