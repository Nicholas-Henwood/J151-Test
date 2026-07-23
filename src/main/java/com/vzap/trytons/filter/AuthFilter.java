package com.vzap.trytons.filter;

import com.vzap.trytons.dao.auth.UserDAO;
import com.vzap.trytons.exceptions.AuthenticationException;
import com.vzap.trytons.model.auth.User;
import com.vzap.trytons.security.AuthPrincipal;
import com.vzap.trytons.security.AuthTokenPayload;
import com.vzap.trytons.annotations.Authenticated;
import com.vzap.trytons.util.AuthTokenUtil;
import jakarta.annotation.Priority;
import jakarta.inject.Inject;
import jakarta.ws.rs.Priorities;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.SecurityContext;
import jakarta.ws.rs.ext.Provider;

import java.io.IOException;
import java.util.UUID;

@Provider
@Authenticated
@Priority(Priorities.AUTHENTICATION)
public class AuthFilter implements ContainerRequestFilter {
    public static final String CURRENT_USER_PROPERTY = "currentUser";
    private static final String BEARER_PREFIX = "Bearer ";

    @Inject
    UserDAO userDAO;

    @Override
    public void filter(ContainerRequestContext requestContext) throws IOException {
        String authorisationHeader = requestContext.getHeaderString(HttpHeaders.AUTHORIZATION);

        if (authorisationHeader == null || !authorisationHeader.startsWith(BEARER_PREFIX)){
            throw new AuthenticationException("Invalid token bearer");
        }

        String token = authorisationHeader.substring(BEARER_PREFIX.length());

        if (token.isBlank()){
            throw new AuthenticationException("Bearer token cannot be blank");
        }

        AuthTokenPayload payload =  AuthTokenUtil.validateToken(token);

        UUID userId = payload.getUserId();

        User validatedUser =  userDAO.getUserById(userId).orElseThrow(() ->new AuthenticationException("This user does not exist"));
        if (!Boolean.TRUE.equals(validatedUser.getIsActive())){
            throw new AuthenticationException("User is no longer active, token is invalid");
        }

        AuthPrincipal principal = AuthPrincipal.builder().userId(userId)
                .username(validatedUser.getUsername())
                .email(validatedUser.getEmail())
                .role(validatedUser.getRole())
                .build();

        requestContext.setProperty(CURRENT_USER_PROPERTY,principal);
        SecurityContext originalSecurityContext = requestContext.getSecurityContext();
        requestContext.setSecurityContext(new SecurityContext() {
            @Override
            public AuthPrincipal getUserPrincipal() {
                return principal;
            }

            @Override
            public boolean isUserInRole(String role) {
                return principal.getRole() != null && principal.getRole().name().equalsIgnoreCase(role);
            }

            @Override
            public boolean isSecure() {
                return originalSecurityContext != null && originalSecurityContext.isSecure();
            }

            @Override
            public String getAuthenticationScheme() {
                return "Bearer";
            }
        });
    }
}