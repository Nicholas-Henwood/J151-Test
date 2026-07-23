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
public class ClubRestClient {
    private final String LIST_CLUBS = "/club";
    private final String GET_CLUB_BY_ID = "/club";
    private final String CREATE_CLUB = "/club";
    private final String UPDATE_CLUB = "/club";

    private static final Logger LOG = Logger.getLogger(ClubRestClient.class.getName());

    @Inject
    private APIClient apiClient;

    public Optional<List<ClubResponse>> listClubs() {
        Optional<ClubResponse[]> response = apiClient.get(LIST_CLUBS, ClubResponse[].class);
        if(response.isEmpty()){
            LOG.log(Level.SEVERE, "Unable list Clubs");
        }
        return response.map(clubs -> new ArrayList<>(Arrays.asList(clubs)));
    }

    public Optional<ClubResponse> getClubById(UUID clubId) {
        String path = GET_CLUB_BY_ID + "/" + encode(clubId.toString());
        Optional<ClubResponse> response = apiClient.get(path,ClubResponse.class);
        if(response.isEmpty()){
            LOG.log(Level.SEVERE, "Unable find Club");
        }
        return response;
    }

    public Optional<ClubResponse> createClub(ClubRequest request){
        Optional<ClubResponse> response = apiClient.post(CREATE_CLUB,request,ClubResponse.class);
        if(response.isEmpty()){
            LOG.log(Level.SEVERE, "Unable create Club");
        }
        return response;
    }

    public Optional<ClubResponse> updateClub(UUID clubId,ClubRequest request){
        String path = UPDATE_CLUB + "/" + encode(clubId.toString());
        Optional<ClubResponse> response = apiClient.put(path,request,ClubResponse.class);
        if(response.isEmpty()){
            LOG.log(Level.SEVERE, "Unable to update Club");
        }
        return response;
    }
    public String encode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }
}
