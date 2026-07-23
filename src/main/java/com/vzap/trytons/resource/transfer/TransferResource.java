package com.vzap.trytons.resource.transfer;

import com.vzap.trytons.annotations.Authenticated;
import com.vzap.trytons.dto.transfer.TransferRequestDTO;
import com.vzap.trytons.dto.transfer.TransferResponseDTO;
import com.vzap.trytons.exceptions.AuthenticationException;
import com.vzap.trytons.filter.AuthFilter;
import com.vzap.trytons.security.AuthPrincipal;
import com.vzap.trytons.service.transfer.TransferService;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.util.List;

@Authenticated
@Path("/transfers")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class TransferResource {
    @Inject
    private TransferService transferService;

    @POST
    public Response executeTransfer(
            @Valid TransferRequestDTO request,
            @Context ContainerRequestContext requestContext) {
        String actorUserId = currentUserId(requestContext);

        TransferResponseDTO response = transferService.executeTransfer(actorUserId, request);

        return Response.ok(response)
                .build();
    }

    @GET
    @Path("/{teamId}/history")
    public Response listTransferHistory(@PathParam("teamId") String teamId, @Context ContainerRequestContext requestContext) {
        String actorUserId = currentUserId(requestContext);

        List<TransferResponseDTO> history = transferService.listTransferHistory(actorUserId, teamId);

        return Response.ok(history)
                .build();
    }

    private String currentUserId(ContainerRequestContext requestContext) {
        Object currentUser = requestContext.getProperty(AuthFilter.CURRENT_USER_PROPERTY);

        if (!(currentUser instanceof AuthPrincipal principal) || principal.getUserId() == null) {
            throw new AuthenticationException("Authentication required");
        }

        return principal.getUserId().toString();
    }
}
