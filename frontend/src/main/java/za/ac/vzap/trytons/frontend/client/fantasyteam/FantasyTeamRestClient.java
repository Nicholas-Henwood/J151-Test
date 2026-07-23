package za.ac.vzap.trytons.frontend.client.fantasyteam;

import jakarta.enterprise.context.Dependent;
import jakarta.inject.Inject;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Optional;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import za.ac.vzap.trytons.frontend.client.shared.APIClient;

@Dependent
public class FantasyTeamRestClient {
    private static final String FANTASY_TEAM_PATH = "/fantasy-team";
    private static final String OWN_TEAM_PATH = "/own";
    private static final String MY_TEAM_PATH = "/mine";
    private static final String OPPONENT_TEAM_PATH = "/opponent";
    private static final Logger LOG = Logger.getLogger(FantasyTeamRestClient.class.getName());

    @Inject
    private APIClient apiClient;
    public Optional<FantasyTeamResponse> createTeam(FantasyTeamRequest request){
        Optional<FantasyTeamResponse> response = apiClient.post(FANTASY_TEAM_PATH,request,FantasyTeamResponse.class);
        if(response.isEmpty()){
            LOG.log(Level.WARNING, "Unable to create fantasy team.");
        }
        return response;
    }

    /**
     * The signed-in user's own team, or empty when they have not created one.
     * The backend answers 404 in that case, which is a normal state for a new
     * account rather than a failure — so this is logged at FINE, not WARNING.
     */
    public Optional<FantasyTeamResponse> getMyTeam(){
        Optional<FantasyTeamResponse> response = apiClient.get(FANTASY_TEAM_PATH + MY_TEAM_PATH, FantasyTeamResponse.class);
        if(response.isEmpty()){
            LOG.log(Level.FINE, "No fantasy team for the current user.");
        }
        return response;
    }

    public Optional<ViewOwnTeamResponse> viewOwnTeam(UUID teamId){
        String path = FANTASY_TEAM_PATH + OWN_TEAM_PATH + "/" + encode(teamId.toString());
        Optional<ViewOwnTeamResponse> response = apiClient.get(path, ViewOwnTeamResponse.class);
        if(response.isEmpty()){
            LOG.log(Level.WARNING, "Unable to view own fantasy team.");
        }
        return response;
    }
    public Optional<ViewOpponentTeamResponse> viewOpponentTeam(UUID teamId){
        String path = FANTASY_TEAM_PATH + OPPONENT_TEAM_PATH + "/" + encode(teamId.toString());
        Optional<ViewOpponentTeamResponse> response = apiClient.get(path, ViewOpponentTeamResponse.class);
        if(response.isEmpty()){
            LOG.log(Level.WARNING, "Unable to view opponent fantasy team.");
        }
        return response;
    }

    public Optional<FantasyTeamResponse> updateTeam(UUID teamId, FantasyTeamRequest request){
        String path = FANTASY_TEAM_PATH + "/" + encode(teamId.toString());
        Optional<FantasyTeamResponse> response = apiClient.put(path, request,FantasyTeamResponse.class);
        if(response.isEmpty()){
            LOG.log(Level.WARNING, "Unable to update fantasy team.");
        }
        return response;
    }

    public String encode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }
}
