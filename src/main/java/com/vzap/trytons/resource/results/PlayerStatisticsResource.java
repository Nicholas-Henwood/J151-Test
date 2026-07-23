package com.vzap.trytons.resource.results;

import com.vzap.trytons.annotations.Authenticated;
import com.vzap.trytons.dto.results.PlayerStatisticsRequestDTO;
import com.vzap.trytons.dto.results.PlayerStatisticsResponseDTO;
import com.vzap.trytons.exceptions.AuthenticationException;
import com.vzap.trytons.filter.AuthFilter;
import com.vzap.trytons.security.AuthPrincipal;
import com.vzap.trytons.service.results.PlayerStatisticsService;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.util.List;
import java.util.UUID;

@RequestScoped
@Path("/player-statistics")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class PlayerStatisticsResource {
    @Inject
    private PlayerStatisticsService playerStatisticsService;

    @POST
    @Authenticated
    public Response captureStatistic(PlayerStatisticsRequestDTO request, @Context ContainerRequestContext containerRequestContext) {
        UUID actorUserId = currentUserId(containerRequestContext);
        PlayerStatisticsResponseDTO response = playerStatisticsService.captureStatistic(actorUserId, request);
        return Response.ok(response).build();
    }

    @GET
    @Path("/result/{resultId}")
    public Response listResultStatistics(@PathParam("resultId") UUID resultId) {
        List<PlayerStatisticsResponseDTO> playersStatistics;
        playersStatistics = playerStatisticsService.listResultStatistics(resultId);

        return Response.ok(playersStatistics).build();
    }

    @GET
    @Path("/result/{resultId}/team/{teamId}")
    public Response listResultStatisticsForTeam(@PathParam("resultId") UUID resultId, @PathParam("teamId") UUID teamId) {
        List<PlayerStatisticsResponseDTO> playersStatistics = playerStatisticsService.listResultStatisticsForTeam(resultId, teamId);

        return Response.ok(playersStatistics)
                .build();
    }

    private UUID currentUserId(ContainerRequestContext requestContext) {
        Object currentUser = requestContext.getProperty(AuthFilter.CURRENT_USER_PROPERTY);
        if (!(currentUser instanceof AuthPrincipal principal) || principal.getUserId() == null) {
            throw new AuthenticationException("Authentication required");
        }
        return principal.getUserId();
    }
}
