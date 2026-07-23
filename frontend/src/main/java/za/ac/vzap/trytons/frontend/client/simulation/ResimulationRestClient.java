package za.ac.vzap.trytons.frontend.client.simulation;

import jakarta.enterprise.context.Dependent;
import jakarta.inject.Inject;
import za.ac.vzap.trytons.frontend.client.shared.APIClient;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

@Dependent
public class ResimulationRestClient {

    private static final String RESIMULATIONS_PATH = "/resimulations";

    private static final Logger LOG = Logger.getLogger(ResimulationRestClient.class.getName());

    @Inject
    private APIClient apiClient;

    public Optional<ResimulationResponse> resimulateFixture(ResimulationRequest request) {
        if (request == null || request.getFixtureId() == null) {
            LOG.log(Level.WARNING, "Fixture id is required to trigger a resimulation.");
            return Optional.empty();
        }

        Optional<ResimulationResponse> response = apiClient.post(RESIMULATIONS_PATH, request, ResimulationResponse.class);
        if (response.isEmpty()) {
            LOG.log(Level.WARNING, "Unable to trigger a resimulation.");
        }
        return response;
    }

    public Optional<List<ResimulationResponse>> listResimulationsForFixture(UUID fixtureId) {
        if (fixtureId == null) {
            LOG.log(Level.WARNING, "Fixture id is required to list resimulations.");
            return Optional.empty();
        }

        String path = RESIMULATIONS_PATH + "/fixture/" + encode(fixtureId.toString());
        Optional<ResimulationResponse[]> response = apiClient.get(path, ResimulationResponse[].class);
        if (response.isEmpty()) {
            LOG.log(Level.WARNING, "Unable to list resimulations for fixture.");
        }
        return response.map(resimulations -> new ArrayList<>(Arrays.asList(resimulations)));
    }

    private String encode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }
}
