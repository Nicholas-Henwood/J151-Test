package za.ac.vzap.trytons.frontend.client.results;

import jakarta.enterprise.context.Dependent;
import jakarta.inject.Inject;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;
import za.ac.vzap.trytons.frontend.client.shared.APIClient;

@Dependent
public class MatchTeamScoreRestClient {
    private static final String MATCH_TEAM_SCORES_PATH = "/match-team-scores";

    private static final Logger LOG = Logger.getLogger(MatchTeamScoreRestClient.class.getName());

    @Inject
    private APIClient apiClient;

    public Optional<MatchTeamScoreResponse> getMatchTeamScoreById(String scoreId) {
        if (isBlank(scoreId)) {
            LOG.log(Level.WARNING, "Score id is required to get a match team score.");
            return Optional.empty();
        }

        String path = MATCH_TEAM_SCORES_PATH + "/" + encode(scoreId);
        Optional<MatchTeamScoreResponse> response = apiClient.get(path, MatchTeamScoreResponse.class);
        if (response.isEmpty()) {
            LOG.log(Level.WARNING, "Unable to get match team score.");
        }
        return response;
    }

    public Optional<List<MatchTeamScoreResponse>> listMatchTeamScoresForResult(String resultId) {
        if (isBlank(resultId)) {
            LOG.log(Level.WARNING, "Result id is required to list match team scores.");
            return Optional.empty();
        }

        String path = MATCH_TEAM_SCORES_PATH + "/result/" + encode(resultId);
        Optional<MatchTeamScoreResponse[]> response = apiClient.get(path, MatchTeamScoreResponse[].class);
        if (response.isEmpty()) {
            LOG.log(Level.WARNING, "Unable to list match team scores for result.");
        }
        return response.map(scores -> new ArrayList<>(Arrays.asList(scores)));
    }

    public Optional<MatchTeamScoreResponse> getMatchTeamScoreForResultSide(String resultId, String teamSide) {
        if (isBlank(resultId) || isBlank(teamSide)) {
            LOG.log(Level.WARNING, "Result id and team side are required to get a match team score.");
            return Optional.empty();
        }

        String path = MATCH_TEAM_SCORES_PATH + "/result/" + encode(resultId) + "/side/" + encode(teamSide);
        Optional<MatchTeamScoreResponse> response = apiClient.get(path, MatchTeamScoreResponse.class);
        if (response.isEmpty()) {
            LOG.log(Level.WARNING, "Unable to get match team score for result side.");
        }
        return response;
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    public String encode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }
}
