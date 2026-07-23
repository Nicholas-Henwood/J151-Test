package com.vzap.trytons.resource.simulation;

import com.vzap.trytons.annotations.AdminOnly;
import com.vzap.trytons.annotations.Authenticated;
import com.vzap.trytons.dto.simulation.ResimulationRequestDTO;
import com.vzap.trytons.dto.simulation.ResimulationResponseDTO;
import com.vzap.trytons.exceptions.AuthenticationException;
import com.vzap.trytons.filter.AuthFilter;
import com.vzap.trytons.security.AuthPrincipal;
import com.vzap.trytons.service.simulation.ControlledResimulationService;
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
@Authenticated
@AdminOnly
@Path("/resimulations")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class ControlledResimulationResource {
    @Inject
    private ControlledResimulationService controlledResimulationService;

    @Context
    private ContainerRequestContext requestContext;

    @POST
    public Response resimulateFixture(ResimulationRequestDTO request) {
        UUID actorUserId = currentUserId();
        ResimulationResponseDTO response = controlledResimulationService.resimulateFixture(actorUserId, request);

        return Response.status(Response.Status.CREATED)
                .entity(response)
                .build();
    }

    @GET
    @Path("/fixture/{fixtureId}")
    public Response listResimulationsForFixture(@PathParam("fixtureId") UUID fixtureId) {
        List<ResimulationResponseDTO> responseList = controlledResimulationService.listResimulationsForFixture(fixtureId);

        return Response.status(Response.Status.OK)
                .entity(responseList)
                .build();
    }

    private UUID currentUserId() {
        AuthPrincipal principal = (AuthPrincipal) requestContext.getProperty(AuthFilter.CURRENT_USER_PROPERTY);
        if (principal == null) {
            throw new AuthenticationException("The authenticated user could not be identified.");
        }
        return principal.getUserId();
    }
}