package za.ac.vzap.trytons.frontend.client.round;

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
public class RoundRestClient {

    private static final String ROUNDS_PATH = "/rounds";
    private static final String CURRENT_OPEN_PATH = "/rounds/current-open";

    private static final Logger LOG = Logger.getLogger(RoundRestClient.class.getName());

    @Inject
    private APIClient apiClient;

    public Optional<List<RoundResponse>> listRounds() {
        Optional<RoundResponse[]> response = apiClient.get(ROUNDS_PATH, RoundResponse[].class);
        if (response.isEmpty()) {
            LOG.log(Level.WARNING, "Unable to list rounds.");
        }
        return response.map(rounds -> new ArrayList<>(Arrays.asList(rounds)));
    }

    public Optional<List<RoundResponse>> listRoundsByStatus(String status) {
        if (isBlank(status)) {
            LOG.log(Level.WARNING, "Status is required to list rounds by status.");
            return Optional.empty();
        }
        String path = ROUNDS_PATH + "?status=" + encode(status);
        Optional<RoundResponse[]> response = apiClient.get(path, RoundResponse[].class);
        if (response.isEmpty()) {
            LOG.log(Level.WARNING, "Unable to list rounds by status.");
        }
        return response.map(rounds -> new ArrayList<>(Arrays.asList(rounds)));
    }

    public Optional<RoundResponse> getCurrentOpenRound() {
        Optional<RoundResponse> response = apiClient.get(CURRENT_OPEN_PATH, RoundResponse.class);
        if (response.isEmpty()) {
            LOG.log(Level.WARNING, "Unable to get current open round.");
        }
        return response;
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    private String encode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }
}
