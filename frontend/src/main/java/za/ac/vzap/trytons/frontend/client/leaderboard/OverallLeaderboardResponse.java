package za.ac.vzap.trytons.frontend.client.leaderboard;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class OverallLeaderboardResponse {
    private String season;
    private List<LeaderboardEntryResponse> standings;
}
