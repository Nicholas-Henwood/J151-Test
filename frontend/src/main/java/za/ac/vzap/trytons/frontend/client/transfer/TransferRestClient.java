package za.ac.vzap.trytons.frontend.client.transfer;

import jakarta.enterprise.context.Dependent;
import jakarta.inject.Inject;
import jakarta.ws.rs.core.GenericType;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import za.ac.vzap.trytons.frontend.client.shared.APIClient;
import za.ac.vzap.trytons.frontend.client.fixture.LockStatusResponse;

@Dependent
public class TransferRestClient {
    private static final String TRANSFERS_PATH = "/transfers";
    private static final String LOCK_STATUS_PATH = "/lock-status";
    private static final String RECOMMENDATIONS_PATH = "/transfer-recommendations";

    private static final Logger LOG = Logger.getLogger(TransferRestClient.class.getName());

    @Inject
    private APIClient apiClient;

    public Optional<TransferResponse> executeTransfer(TransferRequest request) {
        if (!TransferRequestValidator.isValid(request)) {
            LOG.log(Level.WARNING, "Transfer request is invalid.");
            return Optional.empty();
        }
        Optional<TransferResponse> response = apiClient.post(TRANSFERS_PATH, request, TransferResponse.class);
        if (response.isEmpty()) {
            LOG.log(Level.WARNING, "Unable to execute transfer.");
        }
        return response;
    }

    public Optional<List<TransferResponse>> getTransferHistory(String teamId) {
        if (isBlank(teamId)) {
            LOG.log(Level.WARNING, "Team id is required to get transfer history.");
            return Optional.empty();
        }

        String path = TRANSFERS_PATH + "/" + encode(teamId) + "/history";
        Optional<TransferResponse[]> response = apiClient.get(path, TransferResponse[].class);

        if (response.isEmpty()) {
            LOG.log(Level.WARNING, "Unable to get transfer history.");
        }
        return response.map(transfers -> new ArrayList<>(Arrays.asList(transfers)));
    }

    public Optional<LockStatusResponse> getLockStatus(String roundId) {
        if (isBlank(roundId)) {
            LOG.log(Level.WARNING, "Round id is required to get lock status.");
            return Optional.empty();
        }

        String path = LOCK_STATUS_PATH + "/" + roundId;
        Optional<LockStatusResponse> response = apiClient.get(path, LockStatusResponse.class);

        if (response.isEmpty()) {
            LOG.log(Level.WARNING, "Unable to get lock status.");
        }
        return response;
    }


    public Optional<za.ac.vzap.trytons.frontend.client.fixture.DeadlineStatusResponse> getDeadlineStatus(String roundId) {
        if (isBlank(roundId)) {
            LOG.log(Level.WARNING, "Round id is required to get deadline status.");
            return Optional.empty();
        }

        String path = LOCK_STATUS_PATH + "/deadline/" + encode(roundId);
        Optional<za.ac.vzap.trytons.frontend.client.fixture.DeadlineStatusResponse> response = apiClient.get(path, za.ac.vzap.trytons.frontend.client.fixture.DeadlineStatusResponse.class);

        if (response.isEmpty()) {
            LOG.log(Level.WARNING, "Unable to get deadline status.");
        }
        return response;
    }

    public Optional<TransferRecommendationResponse> getTransferRecommendation(String teamId, String currentPlayerId) {
        if (isBlank(teamId)) {
            LOG.log(Level.WARNING, "Team id is required to get transfer recommendation.");
            return Optional.empty();
        }

        TransferRecommendationRequest request = new TransferRecommendationRequest();
        request.setTeamId(UUID.fromString(teamId));
        if(!isBlank(currentPlayerId)){
            request.setCurrentPlayerId(UUID.fromString(currentPlayerId));
        }

        Optional<TransferRecommendationResponse> response = apiClient.post(RECOMMENDATIONS_PATH, request, TransferRecommendationResponse.class);
        if(response.isEmpty()) {
            LOG.log(Level.WARNING, "Unable to get transfer recommendation.");
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