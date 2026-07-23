package com.vzap.trytons.resource.catalog;

import com.vzap.trytons.annotations.AdminOnly;
import com.vzap.trytons.annotations.Authenticated;
import com.vzap.trytons.dto.catalog.ClubRequestDTO;
import com.vzap.trytons.dto.catalog.ClubResponseDTO;
import com.vzap.trytons.service.catalog.ClubService;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;

import java.net.URI;
import java.util.UUID;

@Path("/club")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class ClubResource {
    @Inject
    private ClubService clubService;

    @GET
    public Response listClubs() {
        return Response.ok(clubService.getAllClubs()).build();
    }
    @GET
    @Path("/{id}")
    public Response getClubById(@PathParam("id") UUID id) {
        return Response.ok(clubService.getClub(id)).build();
    }

    @POST
    @Authenticated
    @AdminOnly
    public Response createClub(@Valid ClubRequestDTO request, @Context UriInfo uriInfo) {
        ClubResponseDTO created = clubService.createClub(request);
        URI location = uriInfo.getAbsolutePathBuilder().path(created.getClubId().toString()).build();
        return Response.created(location).entity(created).build();
    }

    @PUT
    @Path("/{id}")
    @Authenticated
    @AdminOnly
    public Response updateClub(@PathParam("id") UUID id, @Valid ClubRequestDTO request) {
        return Response.ok(clubService.updateClub(id, request)).build();
    }
}
