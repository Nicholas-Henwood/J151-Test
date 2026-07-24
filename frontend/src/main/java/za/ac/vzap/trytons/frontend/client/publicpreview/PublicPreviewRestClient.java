package za.ac.vzap.trytons.frontend.client.publicpreview;

import jakarta.enterprise.context.Dependent;
import jakarta.inject.Inject;
import za.ac.vzap.trytons.frontend.client.leaderboard.LeaderboardEntryResponse;
import za.ac.vzap.trytons.frontend.client.shared.APIClient;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Fetches the unauthenticated marketing previews for the landing page.
 * These hit the backend's /public endpoints; APIClient sends no bearer token
 * when there is no session, which is exactly what those endpoints expect.
 */
@Dependent
public class PublicPreviewRestClient {

    private static final String PUBLIC_LEAGUES_PATH = "/public/leagues";
    private static final String PUBLIC_LEADERBOARD_PATH = "/public/leaderboard";

    private static final Logger LOG = Logger.getLogger(PublicPreviewRestClient.class.getName());

    @Inject
    private APIClient apiClient;

    public List<PublicLeagueResponse> listPublicLeagues() {
        Optional<PublicLeagueResponse[]> response = apiClient.get(PUBLIC_LEAGUES_PATH, PublicLeagueResponse[].class);
        if (response.isEmpty()) {
            LOG.log(Level.WARNING, "Unable to load public leagues preview.");
        }
        return response.map(leagues -> new ArrayList<>(Arrays.asList(leagues)))
                .orElseGet(ArrayList::new);
    }

    public List<LeaderboardEntryResponse> getPublicLeaderboard(int limit) {
        String path = PUBLIC_LEADERBOARD_PATH + "?limit=" + limit;
        Optional<LeaderboardEntryResponse[]> response = apiClient.get(path, LeaderboardEntryResponse[].class);
        if (response.isEmpty()) {
            LOG.log(Level.WARNING, "Unable to load public leaderboard preview.");
        }
        return response.map(entries -> new ArrayList<>(Arrays.asList(entries)))
                .orElseGet(ArrayList::new);
    }
}
