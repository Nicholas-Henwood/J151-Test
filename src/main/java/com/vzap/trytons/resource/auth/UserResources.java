package com.vzap.trytons.resource.auth;

import com.vzap.trytons.dto.auth.RegisteredUserRequestDTO;
import com.vzap.trytons.dto.auth.RegisteredUserResponseDTO;
import com.vzap.trytons.enums.UserRole;
import com.vzap.trytons.model.auth.RegisteredUser;
import com.vzap.trytons.service.auth.RegisteredUserServices;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.*;

import java.net.URI;

@Path("/users")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class UserResources {
    @Inject
    private RegisteredUserServices registeredUserServices;

    @POST
    public Response registerUser(@Valid RegisteredUserRequestDTO userRequest, @Context UriInfo uriInfo) {
        RegisteredUser created = registeredUserServices.registerUser(userRequest);
        RegisteredUserResponseDTO body = toResponse(created);
        URI location = uriInfo.getAbsolutePathBuilder().path(created.getUsername()).build();
        return Response.created(location).entity(body).build();
    }

    public RegisteredUserResponseDTO toResponse(RegisteredUser created) {
        return new RegisteredUserResponseDTO(created.getUserId(), created.getUsername(), UserRole.REGISTERED_USER ,created.getRegistrationStatus()
        );
    }
}
