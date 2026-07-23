package com.vzap.trytons.resource.catalog;

import com.vzap.trytons.annotations.AdminOnly;
import com.vzap.trytons.annotations.Authenticated;
import com.vzap.trytons.dto.catalog.PlayerRequestDTO;
import com.vzap.trytons.dto.catalog.PlayerResponseDTO;
import com.vzap.trytons.dto.catalog.PlayerAvailabilityRequestDTO;
import com.vzap.trytons.dto.catalog.PlayerAvailabilityResponseDTO;
import com.vzap.trytons.filter.AuthFilter;
import com.vzap.trytons.security.AuthPrincipal;
import com.vzap.trytons.service.catalog.PlayerAvailabilityService;
import com.vzap.trytons.service.catalog.PlayerService;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;
import java.net.URI;
import java.util.List;
import java.util.UUID;

@Path("/player")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class PlayerResource {
    @Inject
    private PlayerService playerService;

    @Inject
    private PlayerAvailabilityService playerAvailabilityService;

    @Context
    private ContainerRequestContext requestContext;

    private UUID getCurrentUserId() {
        AuthPrincipal principal = (AuthPrincipal) requestContext.getProperty(AuthFilter.CURRENT_USER_PROPERTY);
        return principal != null ? principal.getUserId() : null;
    }

    @GET
    public Response listPlayers(@QueryParam("search") String search, @QueryParam("clubId") UUID clubId, @QueryParam("positionId") UUID positionId ) {
        List<PlayerResponseDTO> players;
        if (search != null || clubId != null || positionId != null) {
            players = playerService.searchPlayers(search, clubId, positionId);
        }else{
            players = playerService.getAllPlayers();
        }
        return Response.ok(players).build();
    }

    @GET
    @Path("/{id}")
    public Response getPlayer(@PathParam("id") UUID id) {
        PlayerResponseDTO player = playerService.getPlayer(id);
        return Response.ok(player).build();
    }

    @POST
    @Authenticated
    @AdminOnly
    public Response createPlayer(@Valid PlayerRequestDTO playerRequestDTO, @Context UriInfo uriInfo) {
        PlayerResponseDTO created = playerService.createPlayer(playerRequestDTO);
        URI location = uriInfo.getAbsolutePathBuilder().path(created.getPlayerId().toString()).build();

        return Response.created(location).entity(created).build();
    }

    @PUT
    @Path("/{id}")
    @Authenticated
    @AdminOnly
    public Response updatePlayer(@PathParam("id") UUID id, @Valid PlayerRequestDTO request){
        PlayerResponseDTO updated = playerService.updatePlayer(id, request);

        return Response.ok(updated).build();
    }



    @PUT
    @Path("/{id}/availability")
    @Authenticated
    @AdminOnly
    public Response setAvailability(@PathParam("id") UUID id, @Valid PlayerAvailabilityRequestDTO request) {
        PlayerAvailabilityResponseDTO updated = playerAvailabilityService.setAvailability(getCurrentUserId(), id, request);

        return Response.ok(updated).build();
    }

}
