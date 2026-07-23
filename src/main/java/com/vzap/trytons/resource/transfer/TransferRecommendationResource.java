package com.vzap.trytons.resource.transfer;

import com.vzap.trytons.annotations.Authenticated;
import com.vzap.trytons.dto.transfer.TransferRecommendationRequestDTO;
import com.vzap.trytons.dto.transfer.TransferRecommendationResponseDTO;
import com.vzap.trytons.exceptions.AuthenticationException;
import com.vzap.trytons.filter.AuthFilter;
import com.vzap.trytons.security.AuthPrincipal;
import com.vzap.trytons.service.transfer.TransferRecommendationService;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.util.UUID;

@Authenticated
@Path("/transfer-recommendations")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class TransferRecommendationResource {
    @Inject
    private TransferRecommendationService transferRecommendationService;

    @POST
    public Response recommendTransfers(@Valid TransferRecommendationRequestDTO request, @Context ContainerRequestContext requestContext) {
        UUID userId = currentUserId(requestContext);
        TransferRecommendationResponseDTO response = transferRecommendationService.recommendTransfers(userId, request);
        return Response.status(Response.Status.OK).entity(response).build();
    }

    private UUID currentUserId(ContainerRequestContext requestContext) {
        Object currentUser = requestContext.getProperty(AuthFilter.CURRENT_USER_PROPERTY);

        if(!(currentUser instanceof AuthPrincipal principal) || principal.getUserId()==null){
            throw new AuthenticationException("Authentication required");
        }

        return  principal.getUserId();
    }
}
