package za.ac.vzap.trytons.frontend.client.catalog;

import jakarta.enterprise.context.Dependent;
import jakarta.inject.Inject;
import za.ac.vzap.trytons.frontend.client.shared.APIClient;

import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

@Dependent
public class PositionRestClient {

    private static final String POSITIONS_PATH = "/position";
    private static final Logger LOG = Logger.getLogger(PositionRestClient.class.getName());

    @Inject
    private APIClient apiClient;

    public Optional<List<PositionResponse>> getAllPositions() {
        String path = POSITIONS_PATH;

        Optional<PositionResponse[]> response = apiClient.get(path, PositionResponse[].class);
        if(response.isEmpty()){
            LOG.log(Level.SEVERE, "Unable to list Positions");
        }
        return response.map(positions -> new ArrayList<>(Arrays.asList(positions)));
    }

    public Optional<PositionResponse> getPositionById(UUID positionId) {
        String path = POSITIONS_PATH + "/" + positionId;
        Optional<PositionResponse> response = apiClient.get(path,PositionResponse.class);
        if(response.isEmpty()){
            LOG.log(Level.SEVERE, "Unable to find Position");
        }
        return response;
    }

    public Optional<PositionResponse> createPosition(PositionRequest request) {
        String path = POSITIONS_PATH;
        Optional<PositionResponse> response = apiClient.post(path,request,PositionResponse.class);
        if(response.isEmpty()){
            LOG.log(Level.SEVERE, "Unable to create Position");
        }
        return response;
    }

    public Optional<PositionResponse> updatePosition(UUID positionId, PositionRequest request) {
        String path = POSITIONS_PATH + "/" + positionId;
        Optional<PositionResponse> response = apiClient.put(path,request,PositionResponse.class);
        if(response.isEmpty()){
            LOG.log(Level.SEVERE, "Unable to update Position");
        }
        return response;
    }
}
