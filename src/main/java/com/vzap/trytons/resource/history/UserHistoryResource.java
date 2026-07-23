package com.vzap.trytons.resource.history;

import com.vzap.trytons.annotations.Authenticated;
import com.vzap.trytons.filter.AuthFilter;
import com.vzap.trytons.security.AuthPrincipal;
import com.vzap.trytons.service.history.UserHistoryService;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.util.UUID;

@Path("/history")
@Produces(MediaType.APPLICATION_JSON)
@Authenticated

public class UserHistoryResource {
    @Inject
    private UserHistoryService userHistoryService;

    @Context
    ContainerRequestContext requestContext;

    @GET
    public Response getUserPointsHistory() {
        UUID requestingUserId = ((AuthPrincipal) requestContext.getProperty(AuthFilter.CURRENT_USER_PROPERTY)).getUserId();
        return Response.ok(userHistoryService.getUserPointsHistory(requestingUserId)).build();
    }

    @GET
    @Path("/weekly")
    public Response getWeeklyPerformance() {
        UUID requestingUserId = ((AuthPrincipal) requestContext.getProperty(AuthFilter.CURRENT_USER_PROPERTY)).getUserId();

        return Response.ok(userHistoryService.getWeeklyPerformance(requestingUserId))
                .build();
    }
}
