package com.vzap.trytons.resource.leaderboard;

import com.vzap.trytons.annotations.Authenticated;
import com.vzap.trytons.dto.leaderboard.LeaderboardEntryResponseDTO;
import com.vzap.trytons.dto.leaderboard.LeaderboardRefreshResultDTO;
import com.vzap.trytons.filter.AuthFilter;
import com.vzap.trytons.security.AuthPrincipal;
import com.vzap.trytons.service.leaderboard.LeaderboardService;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Path("/leaderboard")
@Produces(MediaType.APPLICATION_JSON)
@Authenticated

public class LeaderboardResource {
    @Inject
    private LeaderboardService leaderboardService;

    @Context
    private ContainerRequestContext requestContext;

    @GET
    @Path("/{leagueId}/rankings")
    public Response getLeaderboardForLeague(@PathParam("leagueId") UUID leagueId) {
        UUID requestingUserId = ((AuthPrincipal) requestContext.getProperty(AuthFilter.CURRENT_USER_PROPERTY)).getUserId();
        List<LeaderboardEntryResponseDTO> leaderboard = leaderboardService.getLeaderboardForLeague(leagueId, requestingUserId);

        return Response.ok(leaderboard).build();
    }

    @GET
    @Path("/team/{teamId}")
    public Response getRankingForTeam(@PathParam("teamId") UUID teamId, @QueryParam("leaderboardId") UUID leaderboardId) {
        UUID requestingUserId = ((AuthPrincipal) requestContext.getProperty(AuthFilter.CURRENT_USER_PROPERTY)).getUserId();
        Optional<LeaderboardEntryResponseDTO> result = leaderboardService.getRankingForTeam(teamId, leaderboardId, requestingUserId);
        if (result.isEmpty()) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        return Response.ok(result.get()).build();
    }

    @POST
    @Path("/{leagueId}/refresh")
    public Response refreshLeagueLeaderboard(@PathParam("leagueId") UUID leagueId) {
        UUID actorUserId = ((AuthPrincipal) requestContext.getProperty(AuthFilter.CURRENT_USER_PROPERTY)).getUserId();
        LeaderboardRefreshResultDTO result = leaderboardService.refreshLeagueLeaderboard(actorUserId, leagueId);

        return Response.ok(result).build();
    }

    @GET
    @Path("/master")
    public Response getOverallLeaderboard(){
        UUID requestingUserId = ((AuthPrincipal) requestContext.getProperty(AuthFilter.CURRENT_USER_PROPERTY)).getUserId();
        List<LeaderboardEntryResponseDTO> leaderboard = leaderboardService.getOverallLeaderboard(requestingUserId);

        return Response.ok(leaderboard).build();
    }

    @POST
    @Path("/master/refresh")
    public Response refreshMaterLeaderboard() {
        UUID actorUserId = ((AuthPrincipal) requestContext.getProperty(AuthFilter.CURRENT_USER_PROPERTY)).getUserId();
        LeaderboardRefreshResultDTO result = leaderboardService.refreshOverallLeaderboard(actorUserId);

        return Response.ok(result).build();
    }

}
