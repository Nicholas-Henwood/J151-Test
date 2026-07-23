package com.vzap.trytons.resource.fantasyteam;

import com.vzap.trytons.annotations.Authenticated;
import com.vzap.trytons.dto.fantasyteam.FantasyTeamRequestDTO;
import com.vzap.trytons.dto.fantasyteam.FantasyTeamResponseDTO;
import com.vzap.trytons.dto.fantasyteam.ViewOpponentTeamDTO;
import com.vzap.trytons.dto.fantasyteam.ViewOwnTeamDTO;
import com.vzap.trytons.exceptions.AuthenticationException;
import com.vzap.trytons.filter.AuthFilter;
import com.vzap.trytons.security.AuthPrincipal;
import com.vzap.trytons.service.fantasyteam.FantasyTeamService;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;

import java.net.URI;
import java.util.UUID;

@Authenticated
@Path("/fantasy-team")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)

public class FantasyTeamResource {
    @Inject
    private FantasyTeamService fantasyTeamService;

    @POST
    public Response createTeam(@Valid FantasyTeamRequestDTO request, @Context ContainerRequestContext requestContext, @Context UriInfo uriInfo) {

        UUID userId = currentUserId(requestContext);

        FantasyTeamResponseDTO created = fantasyTeamService.createTeam(userId, request);

        URI location = uriInfo
                .getAbsolutePathBuilder()
                .path(created.getTeamId().toString())
                .build();

        return Response
                .created(location)
                .entity(created)
                .build();
    }

    @GET
    @Path("/own/{teamId}")
    public Response viewOwnTeam(
            @PathParam("teamId") UUID teamId,
            @Context ContainerRequestContext requestContext) {

        UUID userId = currentUserId(requestContext);

        ViewOwnTeamDTO team =
                fantasyTeamService.viewOwnTeam(userId, teamId);

        return Response.ok(team).build();
    }

    @GET
    @Path("/opponent/{teamId}")
    public Response viewOpponentTeam(
            @PathParam("teamId") UUID teamId) {

        ViewOpponentTeamDTO team = fantasyTeamService.viewOpponentTeam(teamId);

        return Response.ok(team).build();
    }

    @PUT
    @Path("/{teamId}")
    public Response updateTeam(@PathParam("teamId") UUID teamId, @Valid FantasyTeamRequestDTO request, @Context ContainerRequestContext requestContext) {
        UUID userId = currentUserId(requestContext);

        FantasyTeamResponseDTO updated = fantasyTeamService.updateTeam(userId, teamId, request);

        return Response.ok(updated)
                .build();
    }

    private UUID currentUserId(
            ContainerRequestContext requestContext) {

        Object currentUser = requestContext.getProperty(AuthFilter.CURRENT_USER_PROPERTY);

        if (!(currentUser instanceof AuthPrincipal principal) || principal.getUserId() == null) {

            throw new AuthenticationException("Authentication required.");
        }

        return principal.getUserId();
    }
}
