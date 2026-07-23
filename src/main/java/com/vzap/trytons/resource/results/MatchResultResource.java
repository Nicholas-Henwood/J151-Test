package com.vzap.trytons.resource.results;

import com.vzap.trytons.annotations.Authenticated;
import com.vzap.trytons.dto.results.MatchResultRequestDTO;
import com.vzap.trytons.dto.results.MatchResultResponseDTO;
import com.vzap.trytons.exceptions.AuthenticationException;
import com.vzap.trytons.filter.AuthFilter;
import com.vzap.trytons.security.AuthPrincipal;
import com.vzap.trytons.service.results.MatchResultService;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
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

import java.util.UUID;

@Authenticated
@Path("/match-results")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class MatchResultResource {
    @Inject
    private MatchResultService matchResultService;

    @POST
    public Response captureResult(@Valid MatchResultRequestDTO request, @Context ContainerRequestContext requestContext) {
        UUID actorUserId = currentUserId(requestContext);
        MatchResultResponseDTO result = matchResultService.captureResult(actorUserId, request);

        return Response.ok(result).build();
    }

    @GET
    @Path("/fixture/{fixtureId}")
    public Response getResult(@PathParam("fixtureId") UUID fixtureId) {
        MatchResultResponseDTO result = matchResultService.getResult(fixtureId);

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
