package com.vzap.trytons.resource.fixture;

import com.vzap.trytons.annotations.Authenticated;
import com.vzap.trytons.dto.shared.ErrorResponseDTO;
import com.vzap.trytons.dto.fixture.RoundResponseDTO;
import com.vzap.trytons.enums.FantasyRoundStatus;
import com.vzap.trytons.service.fixture.RoundService;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.util.List;
import java.util.Optional;

@Path("/rounds")
@Produces(MediaType.APPLICATION_JSON)
@Authenticated
public class RoundResource {
    @Inject
    private RoundService roundService;

    @GET
    public Response listRounds(@QueryParam("status") String status) {
        if (status != null && !status.isBlank()) {
            FantasyRoundStatus parsed;
            try {
                parsed = FantasyRoundStatus.valueOf(status.trim().toUpperCase());
            } catch (IllegalArgumentException e) {
                return Response.status(Response.Status.BAD_REQUEST).entity(ErrorResponseDTO.of("Invalid round status: " + status, "BAD_REQUEST")).build();
            }
            return Response.ok(roundService.listRoundsByStatus(parsed)).build();
        }
        List<RoundResponseDTO> rounds = roundService.listRounds();
        return Response.ok(rounds).build();
    }

    @GET
    @Path("/current-open")
    public Response getCurrentOpenRound() {
        Optional<RoundResponseDTO> round = roundService.getCurrentOpenRound();

        if (round.isEmpty()) {
            return Response.status(Response.Status.NOT_FOUND).entity(ErrorResponseDTO.of("No open round is currently active.", "NOT_FOUND")).build();
        }

        return Response.ok(round.get())
                .build();
    }
}
