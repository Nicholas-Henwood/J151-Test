package za.ac.vzap.trytons.frontend.client.league;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class JoinLeagueRequest {
    private UUID leagueId;
    private String leagueCode;
    private UUID teamId;

    public JoinLeagueRequest(String leagueId, String leagueCode) {
        UUID parsedLeagueId = null;
        if (leagueId != null && !leagueId.isBlank()) {
            try {
                parsedLeagueId = UUID.fromString(leagueId.trim());
            } catch (IllegalArgumentException ignored) {
                // Invalid league id format - leave null so downstream validation treats this as
                // an invalid join request instead of the constructor throwing a 500.
            }
        }
        this.leagueId = parsedLeagueId;
        this.leagueCode = leagueCode;
    }
}