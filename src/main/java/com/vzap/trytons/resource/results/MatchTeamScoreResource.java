package com.vzap.trytons.resource.results;

import com.vzap.trytons.dto.results.MatchTeamScoreResponseDTO;
import com.vzap.trytons.enums.MatchTeamSide;
import com.vzap.trytons.service.results.MatchTeamScoreService;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.util.List;
import java.util.UUID;

@RequestScoped
@Path("/match-team-scores")
@Produces(MediaType.APPLICATION_JSON)
public class MatchTeamScoreResource {

    @Inject
    private MatchTeamScoreService matchTeamScoreService;

    @GET
    @Path("/{scoreId}")
    public Response getMatchTeamScoreById(@PathParam("scoreId") UUID scoreId) {
        MatchTeamScoreResponseDTO score = matchTeamScoreService.getMatchTeamScoreById(scoreId);

        return Response.ok(score)
                .build();
    }

    @GET
    @Path("/result/{resultId}")
    public Response listMatchTeamScoresForResult(@PathParam("resultId") UUID resultId) {
        List<MatchTeamScoreResponseDTO> scores = matchTeamScoreService.listMatchTeamScoresForResult(resultId);

        return Response.ok(scores)
                .build();
    }

    @GET
    @Path("/result/{resultId}/side/{teamSide}")
    public Response getMatchTeamScoreForResultSide(@PathParam("resultId") UUID resultId, @PathParam("teamSide") MatchTeamSide teamSide) {
        MatchTeamScoreResponseDTO score = matchTeamScoreService.getMatchTeamScoreForResultSide(resultId, teamSide);

        return Response.ok(score)
                .build();
    }
}
