package com.vzap.trytons.resource.shared;

import com.vzap.trytons.annotations.Authenticated;
import com.vzap.trytons.filter.AuthFilter;
import com.vzap.trytons.security.AuthPrincipal;
import jakarta.servlet.http.HttpServlet;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.util.Map;

// Protected-endpoint smoke test: confims AuthFilter/@Authenticated is wired end to end by echoing
// back the identity it resolved for the caller (see FTL-Architecture-and-Auth-Flow.md, "Backend architecture").
@Authenticated
@Path("/protected")
@Produces(MediaType.APPLICATION_JSON)
public class ProtectedResource {
    @Context
    private HttpServlet httpServlet;

    @Context
    private ContainerRequestContext requestContext;

    @GET
    @Path("/ping")
    public Response ping() {
        AuthPrincipal principal = (AuthPrincipal) requestContext.getProperty(AuthFilter.CURRENT_USER_PROPERTY);

        return Response.ok(Map.of("message", "Authenticated request received.",
                "userId", principal.getUserId(),
                "username", principal.getUsername(),
                "role", principal.getRole()
        )).build();
    }
}
