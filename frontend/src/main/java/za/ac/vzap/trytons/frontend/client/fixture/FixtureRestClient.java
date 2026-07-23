package za.ac.vzap.trytons.frontend.client.fixture;

import jakarta.enterprise.context.Dependent;
import jakarta.inject.Inject;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import za.ac.vzap.trytons.frontend.client.shared.APIClient;

@Dependent
public class FixtureRestClient {
    private static final String FIXTURE_PATH = "/fixtures";
    private static final Logger LOG = Logger.getLogger(FixtureRestClient.class.getName());

    @Inject
    private APIClient apiClient;

    public Optional<List<FixtureResponse>> listFixtures(String statusFilter) {
        String path = FIXTURE_PATH;
        if(statusFilter != null && !statusFilter.isBlank()) {
            path += "?status=" + encode(statusFilter.trim());
        }
        Optional<FixtureResponse[]> response = apiClient.get(path, FixtureResponse[].class);
        if(response.isEmpty()){
            LOG.log(Level.WARNING, "Unable list fixtures");
        }
        return response.map(fixtures -> new ArrayList<>(Arrays.asList(fixtures)));
    }

    public Optional<FixtureResponse> getFixture(String fixtureId) {
        String path =FIXTURE_PATH + "/" + encode(fixtureId);
        Optional<FixtureResponse> response = apiClient.get(path,FixtureResponse.class);
        if(response.isEmpty()){
            LOG.log(Level.WARNING, "Unable find fixture");
        }
        return response;
    }

    public String encode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }
}
