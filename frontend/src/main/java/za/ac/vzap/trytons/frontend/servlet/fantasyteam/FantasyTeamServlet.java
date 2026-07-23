package za.ac.vzap.trytons.frontend.servlet.fantasyteam;

import jakarta.inject.Inject;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import za.ac.vzap.trytons.frontend.client.catalog.ClubRestClient;
import za.ac.vzap.trytons.frontend.client.catalog.PlayerResponse;
import za.ac.vzap.trytons.frontend.client.catalog.PositionRestClient;
import za.ac.vzap.trytons.frontend.client.fantasyteam.*;
import za.ac.vzap.trytons.frontend.client.catalog.PlayerRestClient;
import java.io.IOException;
import java.util.*;

import za.ac.vzap.trytons.frontend.servlet.shared.AbstractServlet;

@WebServlet(name = "FantasyTeamServlet", urlPatterns = {"/create-team", "/fantasy-team/own", "/fantasy-team/opponent", "/fantasy-team/update"})
public class FantasyTeamServlet extends AbstractServlet{
    private static final String CREATE_TEAM_JSP = "/pages/create-team.jsp";
    private static final String VIEW_OWN_TEAM_JSP = "/pages/view-own-team.jsp";
    private static final String VIEW_OPPONENT_TEAM_JSP = "/pages/view-opponent-team.jsp";

    @Inject
    private FantasyTeamRestClient fantasyTeamRestClient;
    @Inject
    private PlayerRestClient playerRestClient;
    @Inject
    private ClubRestClient clubRestClient;
    @Inject
    private PositionRestClient positionRestClient;

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        if(!requireAuthenticated(request, response)) return;
        String destination = switch (request.getServletPath()){
            case "/fantasy-team/own" ->{
                // The "My Team" nav link carries no teamId, so fall back to the signed-in
                // user's own team. uk_fantasyTeam_owner makes that unambiguous. Same pattern
                // as transfers and fixtures; an explicit ?teamId= still wins.
                Optional<UUID> teamId = parseUuid(request.getParameter("teamId"))
                        .or(() -> fantasyTeamRestClient.getMyTeam().map(FantasyTeamResponse::getTeamId));
                if(teamId.isEmpty()){
                    request.setAttribute("error","You don't have a team yet. Create one to see it here.");
                    yield VIEW_OWN_TEAM_JSP;
                }
                Optional<ViewOwnTeamResponse> team = fantasyTeamRestClient.viewOwnTeam(teamId.get());
                if(team.isPresent()){
                    request.setAttribute("team",team.get());
                }else{
                    request.setAttribute("error","Unable to load your team");
                }
                yield VIEW_OWN_TEAM_JSP;
            }
            case "/fantasy-team/opponent" ->{
                Optional<UUID> teamId = parseUuid(request.getParameter("teamId"));
                if(teamId.isEmpty()){
                    request.setAttribute("error","Team ID is required to view an opponent's team");
                    yield VIEW_OPPONENT_TEAM_JSP;
                }
                Optional<ViewOpponentTeamResponse> team = fantasyTeamRestClient.viewOpponentTeam(UUID.fromString(request.getParameter("teamId")));
                if(team.isPresent()){
                    request.setAttribute("team",team.get());
                }else{
                    request.setAttribute("error","Unable to load that team");
                }
                yield VIEW_OPPONENT_TEAM_JSP;
            }
            case "/fantasy-team/update" ->{
                Optional<UUID> teamId = parseUuid(request.getParameter("teamId"));
                if(teamId.isEmpty()){
                    request.setAttribute("error","Team ID is required to update your team");
                }else {
                    fantasyTeamRestClient.viewOwnTeam(teamId.get()).ifPresentOrElse(team -> request.setAttribute("team",team),() -> request.setAttribute("error","Unable to update your team"));
                    request.setAttribute("team",teamId.get());
                }
                loadPlayerOptions(request);
                yield CREATE_TEAM_JSP;
            }

            default -> {
                loadPlayerOptions(request);
                yield CREATE_TEAM_JSP;
            }
        };
        request.getRequestDispatcher(destination).forward(request, response);

    }
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        if(!requireAuthenticated(request, response)) return;
        String submit = request.getParameter("submit");
        if(submit == null){
            submit = "";
        }
        switch(submit){
            case"","create-team" -> handleCreateTeam(request);
            case "update-team" -> handleUpdateTeam(request);
            default -> request.setAttribute("error","Invalid submit");
        }
        request.getRequestDispatcher(CREATE_TEAM_JSP).forward(request, response);

    }

    private void handleCreateTeam(HttpServletRequest request){
        List<String> validationErrors = new ArrayList<>();
        String teamName = request.getParameter("teamName");
        if(teamName == null || teamName.isBlank()){
            validationErrors.add("Team name is required");
        }
        String[] playerIdParams = request.getParameterValues("playerIds");
        List<UUID> selectedPlayerIds = new ArrayList<>();
        if(playerIdParams != null){
            for(String value : playerIdParams){
                parseUuid(value).ifPresent(selectedPlayerIds::add);
            }
        }
        if(selectedPlayerIds.isEmpty()){
            validationErrors.add("You must select at least one player");
        }
        if(!validationErrors.isEmpty()){
            request.setAttribute("validationErrors",validationErrors);
            loadPlayerOptions(request);
            return;
        }

        FantasyTeamRequest fantasyTeamRequest = buildFantasyTeamRequest(teamName,selectedPlayerIds);
        Optional<FantasyTeamResponse> fantasyTeamResponse = fantasyTeamRestClient.createTeam(fantasyTeamRequest);
        if(fantasyTeamResponse.isPresent()){
            request.setAttribute("message","Team created successfully");
            request.setAttribute("team",fantasyTeamResponse.get());
        }else{
            request.setAttribute("error","Team could not be created. Check your squad rules and budget and try again");
        }
        loadPlayerOptions(request);
    }

    private void handleUpdateTeam(HttpServletRequest request){
        List<String> validationErrors = new ArrayList<>();
        Optional<UUID> teamId = parseUuid(request.getParameter("teamId"));
        if(teamId.isEmpty()){
            validationErrors.add("Team ID is required to update a team.");
        }
        String teamName = request.getParameter("teamName");
        if(teamName == null || teamName.isBlank()){
            validationErrors.add("Team name is required");
        }
        String[] playerIdParams = request.getParameterValues("playerIds");
        List<UUID> selectedPlayerIds = new ArrayList<>();
        if(playerIdParams != null){
            for(String value : playerIdParams){
                parseUuid(value).ifPresent(selectedPlayerIds::add);
            }
        }
        if(selectedPlayerIds.isEmpty()){
            validationErrors.add("You must select at least one player");
        }
        if(!validationErrors.isEmpty()){
            request.setAttribute("validationErrors",validationErrors);
            loadPlayerOptions(request);
            return;
        }
        FantasyTeamRequest fantasyTeamRequest = buildFantasyTeamRequest(teamName, selectedPlayerIds);
        Optional<FantasyTeamResponse> fantasyTeamResponse = fantasyTeamRestClient.updateTeam(teamId.get(),fantasyTeamRequest);
        if(fantasyTeamResponse.isPresent()){
            request.setAttribute("message","Team updated successfully");
            request.setAttribute("team",fantasyTeamResponse.get());
        }else{
            request.setAttribute("error","Team could not be updated. Check your squad rules and budget and try again");
        }
        loadPlayerOptions(request);
    }

    private FantasyTeamRequest buildFantasyTeamRequest(String teamName,List<UUID> selectedPlayerIds){
        List<FantasyTeamPlayerSelectionRequest> selections = new ArrayList<>();
        for(UUID playerId : selectedPlayerIds){
            selections.add(new FantasyTeamPlayerSelectionRequest (playerId, "STARTING", false,false));
        }
        return new FantasyTeamRequest(teamName, selections);
    }

    private void loadPlayerOptions(HttpServletRequest request){
        Optional<List<PlayerResponse>> players = playerRestClient.listPlayers(null,null,null);
        if(players.isPresent()){
            request.setAttribute("players",players.get());
        }else{
            request.setAttribute("error","Unable to load available players");
            request.setAttribute("players",List.of());
        }
        request.setAttribute("clubNamesById", buildClubNameLookup());
        request.setAttribute("positionNamesById",buildPositionNameLookUp());
    }

    private Map<UUID,String> buildClubNameLookup(){
        Map<UUID,String> lookup = new HashMap<>();
        clubRestClient.listClubs().ifPresent(clubs -> clubs.forEach(club -> lookup.put(club.getClubId(), club.getClubName())));
        return lookup;
    }

    private Map<UUID,String> buildPositionNameLookUp(){
        Map<UUID,String> lookup = new HashMap<>();
        positionRestClient.getAllPositions().ifPresent(positions -> positions.forEach(position -> lookup.put(position.getPositionId(), position.getPositionName())));
        return lookup;
    }

}
