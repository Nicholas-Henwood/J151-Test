package com.vzap.trytons.resource.simulation;

import com.vzap.trytons.annotations.AdminOnly;
import com.vzap.trytons.annotations.Authenticated;
import com.vzap.trytons.dto.results.MatchResultResponseDTO;
import com.vzap.trytons.exceptions.AuthenticationException;
import com.vzap.trytons.security.AuthPrincipal;
import com.vzap.trytons.service.simulation.MatchSimulationService;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.SecurityContext;

import java.security.Principal;
import java.util.UUID;

@ApplicationScoped
@Path("/simulations")
@Produces(MediaType.APPLICATION_JSON)
@Authenticated
@AdminOnly
public class SimulationResource {
    @Inject
    private MatchSimulationService matchSimulationService;

    @Context
    private SecurityContext securityContext;

    @POST
    @Path("/fixtures/{fixtureId}")
    public Response simulateFixture(@PathParam("fixtureId") UUID fixtureId) {
        UUID actorUserId = getAuthenticatedActorUserId();

        MatchResultResponseDTO result = matchSimulationService.simulateFixture(actorUserId, fixtureId);

        return Response.ok(result)
                .build();
    }

    private UUID getAuthenticatedActorUserId() {
        if (securityContext == null) {
            throw new AuthenticationException("An authenticated administrator is required.");
        }

        Principal principal = securityContext.getUserPrincipal();

        if (!(principal instanceof AuthPrincipal authPrincipal)) {
            throw new AuthenticationException("The authenticated user could not be resolved.");
        }

        return authPrincipal.getUserId();
    }
}