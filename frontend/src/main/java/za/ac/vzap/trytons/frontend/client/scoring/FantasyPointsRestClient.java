package za.ac.vzap.trytons.frontend.client.scoring;

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
public class FantasyPointsRestClient {
    private static final String FANTASY_POINTS_PATH = "/fantasy-points";
    private static final String FANTASY_POINT_BREAKDOWNS_PATH = "/fantasy-point-breakdowns";

    private static final Logger LOG = Logger.getLogger(FantasyPointsRestClient.class.getName());

    @Inject
    private APIClient apiClient;

    public Optional<FantasyPointsResponse> getFantasyPointsById(String pointsId) {
        if (isBlank(pointsId)) {
            LOG.log(Level.WARNING, "Points id is required to get fantasy points.");
            return Optional.empty();
        }

        String path = FANTASY_POINTS_PATH + "/" + encode(pointsId);
        Optional<FantasyPointsResponse> response = apiClient.get(path, FantasyPointsResponse.class);
        if (response.isEmpty()) {
            LOG.log(Level.WARNING, "Unable to get fantasy points.");
        }
        return response;
    }

    public Optional<List<FantasyPointsResponse>> listFantasyPointsForStat(String statId) {
        if (isBlank(statId)) {
            LOG.log(Level.WARNING, "Stat id is required to list fantasy points.");
            return Optional.empty();
        }

        String path = FANTASY_POINTS_PATH + "/stat/" + encode(statId);
        Optional<FantasyPointsResponse[]> response = apiClient.get(path, FantasyPointsResponse[].class);
        if (response.isEmpty()) {
            LOG.log(Level.WARNING, "Unable to list fantasy points for stat.");
        }
        return response.map(points -> new ArrayList<>(Arrays.asList(points)));
    }

    public Optional<FantasyPointsResponse> getFinalFantasyPointsForStat(String statId) {
        if (isBlank(statId)) {
            LOG.log(Level.WARNING, "Stat id is required to get final fantasy points.");
            return Optional.empty();
        }

        String path = FANTASY_POINTS_PATH + "/stat/" + encode(statId) + "/final";
        Optional<FantasyPointsResponse> response = apiClient.get(path, FantasyPointsResponse.class);
        if (response.isEmpty()) {
            LOG.log(Level.WARNING, "Unable to get final fantasy points for stat.");
        }
        return response;
    }

    public Optional<FantasyPointBreakdownResponse> getBreakdownById(String breakdownId) {
        if (isBlank(breakdownId)) {
            LOG.log(Level.WARNING, "Breakdown id is required to get a fantasy point breakdown.");
            return Optional.empty();
        }

        String path = FANTASY_POINT_BREAKDOWNS_PATH + "/" + encode(breakdownId);
        Optional<FantasyPointBreakdownResponse> response = apiClient.get(path, FantasyPointBreakdownResponse.class);
        if (response.isEmpty()) {
            LOG.log(Level.WARNING, "Unable to get fantasy point breakdown.");
        }
        return response;
    }

    public Optional<List<FantasyPointBreakdownResponse>> listBreakdownsForPoints(String pointsId) {
        if (isBlank(pointsId)) {
            LOG.log(Level.WARNING, "Points id is required to list fantasy point breakdowns.");
            return Optional.empty();
        }

        String path = FANTASY_POINT_BREAKDOWNS_PATH + "/points/" + encode(pointsId);
        Optional<FantasyPointBreakdownResponse[]> response = apiClient.get(path, FantasyPointBreakdownResponse[].class);
        if (response.isEmpty()) {
            LOG.log(Level.WARNING, "Unable to list fantasy point breakdowns for points.");
        }
        return response.map(breakdowns -> new ArrayList<>(Arrays.asList(breakdowns)));
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    public String encode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }
}
