package com.vzap.trytons.resource.admin;

import com.vzap.trytons.annotations.AdminOnly;
import com.vzap.trytons.annotations.Authenticated;
import com.vzap.trytons.dto.admin.AdminUserSearchResponseDTO;
import com.vzap.trytons.dto.admin.AdminUserStatusRequestDTO;
import com.vzap.trytons.dto.admin.AdminUserStatusResponseDTO;
import com.vzap.trytons.exceptions.AuthenticationException;
import com.vzap.trytons.filter.AuthFilter;
import com.vzap.trytons.security.AuthPrincipal;
import com.vzap.trytons.service.admin.AdminUserService;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.util.List;
import java.util.UUID;

@Path("/admin/users")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
@Authenticated
@AdminOnly

public class AdminUserResource {

    @Inject
    AdminUserService adminUserService;

    @GET
    public Response searchUsers(@QueryParam("searchTerm") String searchTerm, @Context ContainerRequestContext requestContext) {
        UUID actorUserId = currentUserId(requestContext);

        List<AdminUserSearchResponseDTO> results = adminUserService.searchUsers(actorUserId, searchTerm);

        return Response.ok(results).build();
    }

    @PUT
    @Path("/{targetUserId}/status")
    @Valid
    public Response updateUserStatus(@PathParam("targetUserId") UUID targetUserId, AdminUserStatusRequestDTO request, @Context ContainerRequestContext requestContext) {
        UUID actorUserId = currentUserId(requestContext);
        AdminUserStatusResponseDTO result = adminUserService.updateUserStatus(actorUserId, targetUserId, request);

        return Response.ok(result).build();
    }

    //getting methods from Timmy Timmy Timmy - the goat

    private UUID currentUserId(ContainerRequestContext requestContext) {
        Object currentUser = requestContext.getProperty(AuthFilter.CURRENT_USER_PROPERTY);

        if (!(currentUser instanceof AuthPrincipal principal) || principal.getUserId() == null) {
            throw new AuthenticationException("Authentication required");
        }

        return principal.getUserId();
    }

}
