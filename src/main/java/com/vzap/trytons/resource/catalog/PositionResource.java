package com.vzap.trytons.resource.catalog;

import com.vzap.trytons.annotations.AdminOnly;
import com.vzap.trytons.annotations.Authenticated;
import com.vzap.trytons.dto.catalog.PositionRequestDTO;
import com.vzap.trytons.dto.catalog.PositionResponseDTO;
import com.vzap.trytons.service.catalog.PositionService;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;

import java.net.URI;
import java.util.UUID;

@Path("/position")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class PositionResource {
    @Inject
    private PositionService positionService;

    @GET
    public Response listPositions(){
        return  Response.ok(positionService.getAllPositions()).build();
    }

    @GET
    @Path("/{id}")
    public Response getPosition(@PathParam("id") UUID id){
        return  Response.ok(positionService.getPosition(id)).build();
    }

    @POST
    @Authenticated
    @AdminOnly
    public Response createPosition(@Valid PositionRequestDTO request, @Context UriInfo uriInfo){
        PositionResponseDTO created = positionService.createPosition(request);
        URI location = uriInfo.getAbsolutePathBuilder().path(created.getPositionId().toString()).build();
        return Response.created(location).entity(created).build();
    }

    @PUT
    @Path("/{id}")
    @Authenticated
    @AdminOnly
    public Response updatePosition(@PathParam("id") UUID id, @Valid PositionRequestDTO request){
        return Response.ok(positionService.updatePosition(id, request)).build();
    }
}
