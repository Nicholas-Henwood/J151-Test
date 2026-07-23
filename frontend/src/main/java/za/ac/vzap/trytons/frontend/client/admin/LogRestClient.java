package za.ac.vzap.trytons.frontend.client.admin;

import jakarta.enterprise.context.Dependent;
import jakarta.inject.Inject;
import za.ac.vzap.trytons.frontend.client.shared.APIClient;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

@Dependent
public class LogRestClient {
    private static final Logger LOG = Logger.getLogger(LogRestClient.class.getName());
    private static final String LOGS_PATH = "/logs";

    @Inject
    private APIClient apiClient;

    public Optional<List<LogResponse>> getRecentLogs(int limit) {
        String path = LOGS_PATH + "?limit=" + limit;
        Optional<LogResponse[]> response = apiClient.get(path, LogResponse[].class);
        if(response.isEmpty()){
            LOG.log(Level.SEVERE, " Unable to retrieve recent logs.");
        }
        return response.map(logs -> new ArrayList<>(Arrays.asList(logs)));
    }
}
