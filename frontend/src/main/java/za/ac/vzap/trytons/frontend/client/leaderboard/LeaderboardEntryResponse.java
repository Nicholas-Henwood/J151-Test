package za.ac.vzap.trytons.frontend.client.leaderboard;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.*;

import java.util.UUID;

/**
 * Tolerates unknown JSON fields: the backend serialises the master-leaderboard
 * score column as "pointsDifference" while this DTO calls it "scoreDifference".
 * Without this, the whole array fails to deserialise (Jackson defaults to
 * FAIL_ON_UNKNOWN_PROPERTIES=true) and the leaderboard renders empty.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder

public class LeaderboardEntryResponse {
    private UUID teamId;
    private String teamName;
    private String owner;
    private int rank;
    private Integer rankMovement;
    private Integer previousRanking;
    private int matchesPlayed, matchesWon, matchesDrawn, matchesLost, pointsFor, pointsAgainst,scoreDifference,leaguePoints,totalFantasyPoints;
}
