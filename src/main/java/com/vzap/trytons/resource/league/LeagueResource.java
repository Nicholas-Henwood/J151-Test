package com.vzap.trytons.resource.league;

import com.vzap.trytons.annotations.Authenticated;
import com.vzap.trytons.dto.league.JoinLeagueRequestDTO;
import com.vzap.trytons.dto.league.JoinLeagueResponseDTO;
import com.vzap.trytons.dto.league.LeagueMemberResponseDTO;
import com.vzap.trytons.dto.league.LeagueRequestDTO;
import com.vzap.trytons.dto.league.LeagueResponseDTO;
import com.vzap.trytons.filter.AuthFilter;
import com.vzap.trytons.security.AuthPrincipal;
import com.vzap.trytons.service.league.LeagueService;
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


@Path("/league")
@Authenticated
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class LeagueResource {
    @Inject
    private LeagueService leagueService;

    @Context
    private ContainerRequestContext request;
    private UUID getCurrentUserId(){
        AuthPrincipal principal = (AuthPrincipal) request.getProperty(AuthFilter.CURRENT_USER_PROPERTY);

        return principal.getUserId();
    }

    @POST
    public Response createLeague(@Valid LeagueRequestDTO request,@Context UriInfo uriInfo) {
        LeagueResponseDTO created = leagueService.createLeague(request,getCurrentUserId());
        URI location = uriInfo.getAbsolutePathBuilder().path(created.getLeagueId().toString()).build();

        return Response.created(location).entity(created).build();
    }

    @GET
    @Path("/{id}")
    public Response getLeague(@PathParam("id") UUID id) {
        LeagueResponseDTO league = leagueService.getLeague(id, getCurrentUserId());

        return Response.ok(league)
                .build();
    }

    @GET
    public Response getAllLeagues() {
        return Response.ok(leagueService.getAllLeagues(getCurrentUserId()))
                .build();
    }

    @POST
    @Path("/join")
    public Response joinLeague(@Valid JoinLeagueRequestDTO request) {
        JoinLeagueResponseDTO response = leagueService.joinLeague(request, getCurrentUserId());

        return Response.status(Response.Status.OK)
                .entity(response)
                .build();
    }

    @GET
    @Path("/{id}/members")
    public Response listMembers(@PathParam("id") String id) {
        List<LeagueMemberResponseDTO> members = leagueService.listMembers(getCurrentUserId().toString(), id);

        return Response.ok(members)
                .build();
    }

    @DELETE
    @Path("/{id}/members/{membershipId}")
    public Response removeMember(@PathParam("id") String id, @PathParam("membershipId") String membershipId) {
        leagueService.removeMember(getCurrentUserId().toString(), id, membershipId);

        return Response.noContent()
                .build();
    }

    @GET
    @Path("/{id}/code")
    public Response getLeagueCode(@PathParam("id") String id) {
        String code = leagueService.getLeagueCode(getCurrentUserId().toString(), id);
        return Response.ok(java.util.Map.of("leagueCode", code))
                .build();
    }

}
