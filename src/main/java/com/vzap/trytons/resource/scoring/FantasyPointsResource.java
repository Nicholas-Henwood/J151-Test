package com.vzap.trytons.resource.scoring;

import com.vzap.trytons.annotations.Authenticated;
import com.vzap.trytons.dto.scoring.FantasyPointsRequestDTO;
import com.vzap.trytons.dto.scoring.FantasyPointsResponseDTO;
import com.vzap.trytons.exceptions.AuthenticationException;
import com.vzap.trytons.filter.AuthFilter;
import com.vzap.trytons.security.AuthPrincipal;
import com.vzap.trytons.service.scoring.FantasyPointsService;
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

@Authenticated
@Path("/fantasy-points")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class FantasyPointsResource {
    @Inject
    FantasyPointsService fantasyPointsService;

    @POST
    @Path("/calculate")
    public Response calculateFantasyPoints(
            FantasyPointsRequestDTO request,
            @Context ContainerRequestContext requestContext) {
        UUID actorUserId = currentUserId(requestContext);

        FantasyPointsResponseDTO result = fantasyPointsService.calculateFantasyPoints(actorUserId, request);

        return Response.ok(result).build();
    }

    @GET
    @Path("/{pointsId}")
    public Response getFantasyPointsById(@PathParam("pointsId") UUID pointsId) {
        FantasyPointsResponseDTO result = fantasyPointsService.getFantasyPointsById(pointsId);

        return Response.ok(result)
                .build();
    }

    @GET
    @Path("/stat/{statId}")
    public Response listFantasyPointsForStat(@PathParam("statId") UUID statId) {
        List<FantasyPointsResponseDTO> results = fantasyPointsService.listFantasyPointsForStat(statId);

        return Response.ok(results)
                .build();
    }

    @GET
    @Path("/stat/{statId}/final")
    public Response getFinalFantasyPointsForStat(@PathParam("statId") UUID statId) {
        FantasyPointsResponseDTO result = fantasyPointsService.getFinalFantasyPointsForStat(statId);

        return Response.ok(result)
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
