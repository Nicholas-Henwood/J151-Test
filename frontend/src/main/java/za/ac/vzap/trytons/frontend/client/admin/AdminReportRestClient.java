package za.ac.vzap.trytons.frontend.client.admin;

import jakarta.enterprise.context.Dependent;
import jakarta.inject.Inject;
import za.ac.vzap.trytons.frontend.client.shared.APIClient;

import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

@Dependent
public class AdminReportRestClient {
    private static final Logger LOG = Logger.getLogger(AdminReportRestClient.class.getName());
    private static final String SYSTEM_REPORTS_PATH = "/system-reports";

    @Inject
    private APIClient apiClient;

    public Optional<SystemReportResponse> generateReport(SystemReportRequest request) {
        Optional<SystemReportResponse> response = apiClient.post(SYSTEM_REPORTS_PATH,request,SystemReportResponse.class);
        if(response.isEmpty()){
            LOG.log(Level.SEVERE, " Unable to generate system report.");
        }
        return response;
    }

    public Optional<List<SystemReportResponse>> getReports() {
        Optional<SystemReportResponse[]> response = apiClient.get(SYSTEM_REPORTS_PATH, SystemReportResponse[].class);
        if(response.isEmpty()){
            LOG.log(Level.SEVERE, " Unable to list system reports.");
        }
        return response.map(reports -> new ArrayList<>(Arrays.asList(reports)));
    }

    public Optional<SystemReportResponse> getReportById(UUID reportId) {
        String path = SYSTEM_REPORTS_PATH + "/" + reportId;
        Optional<SystemReportResponse> response = apiClient.get(path, SystemReportResponse.class);
        if(response.isEmpty()){
            LOG.log(Level.SEVERE, " Unable to retrieve system report.");
        }
        return response;
    }
}
