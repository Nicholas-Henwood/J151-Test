package com.vzap.trytons.resource.auth;

import com.vzap.trytons.annotations.Authenticated;
import com.vzap.trytons.dto.auth.ChangePasswordRequestDTO;
import com.vzap.trytons.dto.auth.ProfileResponseDTO;
import com.vzap.trytons.dto.auth.ProfileUpdateRequestDTO;
import com.vzap.trytons.exceptions.AuthenticationException;
import com.vzap.trytons.filter.AuthFilter;
import com.vzap.trytons.security.AuthPrincipal;
import com.vzap.trytons.service.auth.RegisteredUserServices;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@RequestScoped
@Authenticated
@Path("/users/profile")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class ProfileResource {
    @Inject
    private RegisteredUserServices registeredUserServices;

    @Context
    private ContainerRequestContext requestContext;

    @GET
    public Response getProfile() {
        AuthPrincipal principal = currentPrincipal();
        ProfileResponseDTO response = registeredUserServices.getProfile(principal.getUserId());

        return Response.status(Response.Status.OK)
                .entity(response)
                .build();
    }

    @PUT
    public Response updateProfile(ProfileUpdateRequestDTO request) {
        AuthPrincipal principal = currentPrincipal();
        ProfileResponseDTO response = registeredUserServices.updateProfile(principal.getUserId(), request);

        return Response.status(Response.Status.OK)
                .entity(response)
                .build();
    }

    @POST
    @Path("/change-password")
    public Response changePassword(ChangePasswordRequestDTO request) {
        AuthPrincipal principal = currentPrincipal();
        registeredUserServices.changePassword(principal.getUserId(), request);

        return Response.status(Response.Status.OK).build();
    }

    private AuthPrincipal currentPrincipal() {
        AuthPrincipal principal = (AuthPrincipal) requestContext.getProperty(AuthFilter.CURRENT_USER_PROPERTY);
        if (principal == null) {
            throw new AuthenticationException("The authenticated user could not be identified.");
        }
        return principal;
    }
}
