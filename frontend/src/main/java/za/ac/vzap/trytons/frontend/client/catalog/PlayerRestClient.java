package za.ac.vzap.trytons.frontend.client.catalog;

import jakarta.enterprise.context.Dependent;
import jakarta.inject.Inject;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import za.ac.vzap.trytons.frontend.client.shared.APIClient;

@Dependent
public class PlayerRestClient {
    private final String LIST_PLAYERS ="/player";
    private final String GET_PLAYER = "/player";
    private final String CREATE_PLAYER = "/player";
    private final String UPDATE_PLAYER ="/player";
    private final String PLAYER_AVAILABILITY ="/player";
    private final String IMPORT_PLAYERS ="/player/import";

    private static final Logger LOG = Logger.getLogger(PlayerRestClient.class.getName());
    @Inject
    private APIClient apiClient;

    public Optional<List<PlayerResponse>> listPlayers(String search , UUID clubId , UUID positionId) {
        return listPlayers(search, clubId, positionId, false);
    }

    /**
     * When availableOnly is true, only players currently available for selection are
     * returned — used by the create-team pool so it never offers a player the squad
     * validator would reject on submit.
     */
    public Optional<List<PlayerResponse>> listPlayers(String search , UUID clubId , UUID positionId, boolean availableOnly) {
        StringBuilder path = new StringBuilder(LIST_PLAYERS);
        List<String> params = new ArrayList<>();
        if (search != null && !search.isBlank()) {
            params.add("search=" + encode(search));
        }
        if (clubId != null) {
            params.add("clubId=" + encode(clubId.toString()));
        }
        if (positionId != null) {
            params.add("positionId=" + encode(positionId.toString()));
        }
        if (availableOnly) {
            params.add("available=true");
        }
        if (!params.isEmpty()) {
            path.append("?").append(String.join("&", params));
        }
        Optional<PlayerResponse[]> response = apiClient.get(path.toString(), PlayerResponse[].class);
        if(response.isEmpty()){
            LOG.log(Level.SEVERE, "Unable list player");
        }
        return response.map(players -> new ArrayList<>(Arrays.asList(players)));
    }

    public Optional<PlayerResponse> getPlayer(UUID playerId) {
        String path = GET_PLAYER + "/" + encode(playerId.toString());
        Optional<PlayerResponse> response = apiClient.get(path,PlayerResponse.class);
        if(response.isEmpty()){
            LOG.log(Level.SEVERE, "Unable find player");
        }
        return response;
    }

    public Optional<PlayerResponse> createPlayer(PlayerRequest request) {
        Optional<PlayerResponse> response = apiClient.post(CREATE_PLAYER,request,PlayerResponse.class);
        if(response.isEmpty()){
            LOG.log(Level.SEVERE, "Unable to create player");
        }
        return response;
    }

    public Optional<PlayerResponse> updatePlayer (UUID playerId , PlayerRequest request) {
        String path = UPDATE_PLAYER + "/" + encode(playerId.toString());
        Optional<PlayerResponse> response = apiClient.put(path,request,PlayerResponse.class);
        if(response.isEmpty()){
            LOG.log(Level.SEVERE, "Unable to update player");
        }
        return response;
    }

    /**
     * Triggers a live-feed refresh of the player catalog. The backend re-scrapes its
     * source on every call and takes about a minute, so this is a single deliberate
     * request, never a poll. The endpoint takes no body, so an empty JSON object is
     * sent (JAX-RS rejects a null POST entity).
     */
    public Optional<PlayerImportSummaryResponse> importPlayers() {
        Optional<PlayerImportSummaryResponse> response =
                apiClient.post(IMPORT_PLAYERS, java.util.Map.of(), PlayerImportSummaryResponse.class);
        if (response.isEmpty()) {
            LOG.log(Level.SEVERE, "Unable to import players from live feed");
        }
        return response;
    }

    public Optional<PlayerAvailabilityResponse> setAvailability(UUID playerId, PlayerAvailabilityRequest request) {
        String path = PLAYER_AVAILABILITY + "/" + encode(playerId.toString()) + "/availability";
        Optional<PlayerAvailabilityResponse> response = apiClient.put(path, request, PlayerAvailabilityResponse.class);
        if(response.isEmpty()){
            LOG.log(Level.SEVERE, "Unable to set player availability");
        }
        return response;
    }

    public String encode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }
}
