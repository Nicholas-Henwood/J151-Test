package com.vzap.trytons.filter;

import com.vzap.trytons.annotations.AdminOnly;
import com.vzap.trytons.exceptions.AuthenticationException;
import com.vzap.trytons.exceptions.AuthorisationException;
import com.vzap.trytons.security.AuthPrincipal;
import com.vzap.trytons.util.RoleUtil;
import jakarta.annotation.Priority;
import jakarta.ws.rs.Priorities;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.ext.Provider;

import java.io.IOException;

@Provider
@AdminOnly
@Priority(Priorities.AUTHORIZATION)
public class RoleFilter implements ContainerRequestFilter {

    @Override
    public void filter(ContainerRequestContext requestContext) throws IOException {
        Object currentUserProperty = requestContext.getProperty(AuthFilter.CURRENT_USER_PROPERTY);

        if (!(currentUserProperty instanceof AuthPrincipal principal)) {
            throw new AuthenticationException("Authentication is required.");
        }

        if (!RoleUtil.isAdmin(principal)) {
            throw new AuthorisationException("Admin access required.");
        }
    }
}