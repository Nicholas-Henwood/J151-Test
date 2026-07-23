package com.vzap.trytons.resource.auth;

import com.vzap.trytons.annotations.Authenticated;
import com.vzap.trytons.dto.auth.*;
import com.vzap.trytons.exceptions.ValidationException;
import com.vzap.trytons.filter.AuthFilter;
import com.vzap.trytons.security.AuthPrincipal;
import com.vzap.trytons.service.auth.AuthService;
import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import com.vzap.trytons.model.auth.RegisteredUser;
import com.vzap.trytons.service.auth.RegisteredUserServices;
import jakarta.validation.Valid;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.UriInfo;
import java.net.URI;


@Path("/auth")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class AuthResource {
    @Inject
    private RegisteredUserServices registeredUserServices;

    @Inject
    private AuthService authService;

    @POST
    @Path("/login")
    public Response login(LoginRequestDTO request) {
        if (request == null) {
            throw new ValidationException("Login request is required.");
        }
        LoginResponseDTO response = authService.authenticate(request.getIdentifier(), request.getPassword());
        return Response.ok(response).build();
    }

    @POST
    @Path("/logout")
    @Authenticated
    public Response logout() {
        LogoutResponseDTO response = LogoutResponseDTO.builder().message(authService.logout()).build();
        return Response.ok(response).build();
    }

    @GET
    @Path("/status")
    @Authenticated
    public Response getAuthStatus(@Context ContainerRequestContext requestContext) {
        AuthPrincipal currentUser = (AuthPrincipal) requestContext.getProperty(AuthFilter.CURRENT_USER_PROPERTY);
        AuthStatusResponseDTO response = authService.getAuthStatus(currentUser.getUserId().toString());
        return Response.ok(response).build();
    }

    @POST
    @Path("/register")
    public Response register(@Valid RegisteredUserRequestDTO request, @Context UriInfo uriInfo) {
        RegisteredUser created = registeredUserServices.registerUser(request);
        RegisteredUserResponseDTO response = new RegisteredUserResponseDTO(created.getUserId(), created.getUsername(), created.getRole(), created.getRegistrationStatus());
        URI location = uriInfo.getAbsolutePathBuilder()
                .path(created.getUsername())
                .build();

        return Response.created(location)
                .entity(response)
                .build();
    }
}
