package com.vzap.trytons.resource.scoring;

import com.vzap.trytons.annotations.Authenticated;
import com.vzap.trytons.dto.scoring.ScoringRuleRequestDTO;
import com.vzap.trytons.dto.scoring.ScoringRuleResponseDTO;
import com.vzap.trytons.exceptions.AuthenticationException;
import com.vzap.trytons.filter.AuthFilter;
import com.vzap.trytons.security.AuthPrincipal;
import com.vzap.trytons.service.scoring.ScoringRuleService;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.util.List;
import java.util.UUID;

@Authenticated
@Path("/scoring-rules")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class ScoringRuleResource {
    @Inject
    ScoringRuleService scoringRuleService;

    @GET
    public Response listRules(@QueryParam("season") String season, @Context ContainerRequestContext requestContext) {
        UUID actorUserId = currentUserId(requestContext);

        List<ScoringRuleResponseDTO> results = scoringRuleService.listRules(actorUserId, season);

        return Response.ok(results)
                .build();
    }

    @GET
    @Path("/lock-status")
    public Response seasonLockStatus(@QueryParam("season") String season, @Context ContainerRequestContext requestContext) {
        UUID actorUserId = currentUserId(requestContext);

        boolean locked = scoringRuleService.isSeasonLocked(actorUserId, season);

        return Response.ok(locked)
                .build();
    }

    @POST
    public Response saveRule(@Valid ScoringRuleRequestDTO request, @Context ContainerRequestContext requestContext) {
        UUID actorUserId = currentUserId(requestContext);

        ScoringRuleResponseDTO result = scoringRuleService.saveRule(actorUserId, request);

        return Response.ok(result)
                .build();
    }

    private UUID currentUserId(ContainerRequestContext requestContext) {
        Object currentUser = requestContext.getProperty(AuthFilter.CURRENT_USER_PROPERTY);

        if (!(currentUser instanceof AuthPrincipal principal) || principal.getUserId() == null) {
            throw new AuthenticationException("Authentication required");
        }

        return principal.getUserId();
    }
}
