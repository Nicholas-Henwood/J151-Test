package com.vzap.trytons.resource.fixture;

import com.vzap.trytons.annotations.AdminOnly;
import com.vzap.trytons.annotations.Authenticated;
import com.vzap.trytons.dto.fixture.FixtureRequestDTO;
import com.vzap.trytons.dto.fixture.FixtureResponseDTO;
import com.vzap.trytons.enums.FixtureStatus;
import com.vzap.trytons.filter.AuthFilter;
import com.vzap.trytons.security.AuthPrincipal;
import com.vzap.trytons.service.fixture.FixtureService;
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

@Path("/fixtures")
@Authenticated
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class FixtureResource {
    @Inject
    private FixtureService fixtureService;
    @Context
    private ContainerRequestContext request;

    private UUID getCurrentUserId(){
        AuthPrincipal principal = (AuthPrincipal) request.getProperty(AuthFilter.CURRENT_USER_PROPERTY);
        return principal.getUserId();
    }

    @GET
    public Response listFixtures(@QueryParam("status")FixtureStatus status){
        List<FixtureResponseDTO>fixtures = fixtureService.listFixtures(status);
        return Response.ok(fixtures)
                .build();
    }

    @GET
    @Path("/{fixtureId}")
    public Response getFixture(@PathParam("fixtureId") UUID fixtureId){
        FixtureResponseDTO fixture = fixtureService.getFixture(fixtureId);
        return Response.ok(fixture).build();
    }

    @POST
    @AdminOnly
    public Response createFixture(@Valid FixtureRequestDTO request, @Context UriInfo uriInfo){
        FixtureResponseDTO created = fixtureService.createFixture(getCurrentUserId(),request);
        URI location = uriInfo.getAbsolutePathBuilder().path(created.getFixtureId().toString()).build();
        return Response.created(location)
                .entity(created)
                .build();
    }

    @PUT
    @Path("/{fixtureId}/status")
    @AdminOnly
    public Response updateFixtureStatus(@PathParam("fixtureId") UUID fixtureId, @QueryParam("status") FixtureStatus status){
        FixtureResponseDTO updated = fixtureService.updateFixtureStatus(getCurrentUserId(),fixtureId,status);
        return Response.ok(updated)
                .build();
    }

}
