package za.ac.vzap.trytons.frontend.client.scoring;

import jakarta.enterprise.context.Dependent;
import jakarta.inject.Inject;
import za.ac.vzap.trytons.frontend.client.shared.APIClient;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

@Dependent
public class ScoringRuleRestClient {

    private static final String SCORING_RULES_PATH = "/scoring-rules";

    private static final Logger LOG = Logger.getLogger(ScoringRuleRestClient.class.getName());

    @Inject
    private APIClient apiClient;

    public Optional<List<ScoringRuleResponse>> getScoringRules(String season) {
        if (isBlank(season)) {
            LOG.log(Level.WARNING, "Season is required to get scoring rules.");
            return Optional.empty();
        }

        String path = SCORING_RULES_PATH + "?season=" + encode(season);
        Optional<ScoringRuleResponse[]> response = apiClient.get(path, ScoringRuleResponse[].class);
        if (response.isEmpty()) {
            LOG.log(Level.WARNING, "Unable to get scoring rules.");
        }
        return response.map(rules -> new ArrayList<>(Arrays.asList(rules)));
    }

    public boolean isSeasonLocked(String season) {
        if (isBlank(season)) {
            return false;
        }

        String path = SCORING_RULES_PATH + "/lock-status?season=" + encode(season);
        Optional<Boolean> locked = apiClient.get(path, Boolean.class);
        if (locked.isEmpty()) {
            // Fail open: the database triggers remain the authoritative guard, and a save
            // against a locked season still returns a clear conflict message.
            LOG.log(Level.WARNING, "Unable to determine season lock status for {0}.", season);
        }
        return locked.orElse(false);
    }

    public Optional<ScoringRuleResponse> saveScoringRule(ScoringRuleRequest request) {
        if (request == null) {
            LOG.log(Level.WARNING, "Scoring rule request is required to save a scoring rule.");
            return Optional.empty();
        }

        Optional<ScoringRuleResponse> response = apiClient.post(SCORING_RULES_PATH, request, ScoringRuleResponse.class);
        if (response.isEmpty()) {
            LOG.log(Level.WARNING, "Unable to save scoring rule.");
        }
        return response;
    }

    private String encode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
