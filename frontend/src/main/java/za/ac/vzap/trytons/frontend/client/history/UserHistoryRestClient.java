package za.ac.vzap.trytons.frontend.client.history;

import jakarta.enterprise.context.Dependent;
import jakarta.inject.Inject;
import jakarta.ws.rs.core.GenericType;

import java.util.List;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;
import za.ac.vzap.trytons.frontend.client.shared.APIClient;

@Dependent
public class UserHistoryRestClient {

    private static final String GET_USER_POINTS_HISTORY = "/history";
    private static final String GET_WEEKLY_USER_POINTS_HISTORY = "/history/weekly";

    private static final Logger LOG = Logger.getLogger(UserHistoryRestClient.class.getName());

    @Inject
    private APIClient apiClient;

    public Optional<UserPointsHistoryResponse> getUserPointsHistory() {
        String path = GET_USER_POINTS_HISTORY;
        Optional<UserPointsHistoryResponse> response = apiClient.get(path, UserPointsHistoryResponse.class);
        if (response.isEmpty()){
            LOG.log(Level.SEVERE, "Could not get user history.");
        }
        return response;
    }

    public Optional<List<WeeklyPerformanceResponse>> getWeeklyPerformance() {
        String path = GET_WEEKLY_USER_POINTS_HISTORY;
        Optional<List<WeeklyPerformanceResponse>> response = apiClient.getList(path, new GenericType<List<WeeklyPerformanceResponse>>(){});
        if (response.isEmpty()){
            LOG.log(Level.SEVERE, "Could not get weekly user history.");
        }
        return response;
    }
}