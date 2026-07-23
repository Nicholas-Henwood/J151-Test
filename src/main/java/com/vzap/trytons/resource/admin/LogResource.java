package com.vzap.trytons.resource.admin;

import com.vzap.trytons.annotations.AdminOnly;
import com.vzap.trytons.annotations.Authenticated;
import com.vzap.trytons.dto.admin.LogResponseDTO;
import com.vzap.trytons.exceptions.AuthenticationException;
import com.vzap.trytons.filter.AuthFilter;
import com.vzap.trytons.security.AuthPrincipal;
import com.vzap.trytons.service.admin.LogService;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.util.List;
import java.util.UUID;

@RequestScoped
@Authenticated
@AdminOnly
@Path("/logs")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class LogResource {

    @Inject
    private LogService logService;

    @Context
    private ContainerRequestContext requestContext;

    @GET
    public Response findRecentLogs(@QueryParam("limit") Integer limit) {
        UUID actorUserId = currentUserId();
        int effectiveLimit = limit != null ? limit : 100;
        List<LogResponseDTO> responseList = logService.findRecentLogs(actorUserId, effectiveLimit);

        return Response.status(Response.Status.OK)
                .entity(responseList)
                .build();
    }

    private UUID currentUserId() {
        AuthPrincipal principal = (AuthPrincipal) requestContext.getProperty(AuthFilter.CURRENT_USER_PROPERTY);
        if (principal == null) {
            throw new AuthenticationException("The authenticated user could not be identified.");
        }
        return principal.getUserId();
    }
}
