package com.vzap.trytons.resource.publicpreview;

import com.vzap.trytons.service.leaderboard.LeaderboardService;
import com.vzap.trytons.service.league.LeagueService;
import jakarta.inject.Inject;
import jakarta.ws.rs.DefaultValue;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

/**
 * Unauthenticated, read-only marketing previews for the pre-auth landing page.
 * Deliberately NOT annotated {@code @Authenticated} so the AuthFilter does not run.
 * Only exposes marketing-safe data (no ids, no invite codes).
 */
@Path("/public")
@Produces(MediaType.APPLICATION_JSON)
public class PublicPreviewResource {

    @Inject
    private LeagueService leagueService;

    @Inject
    private LeaderboardService leaderboardService;

    @GET
    @Path("/leagues")
    public Response getPublicLeagues(@QueryParam("limit") @DefaultValue("0") int limit) {
        return Response.ok(leagueService.getPublicLeaguePreviews(limit)).build();
    }

    @GET
    @Path("/leaderboard")
    public Response getPublicLeaderboard(@QueryParam("limit") @DefaultValue("6") int limit) {
        return Response.ok(leaderboardService.getPublicOverallLeaderboard(limit)).build();
    }
}
