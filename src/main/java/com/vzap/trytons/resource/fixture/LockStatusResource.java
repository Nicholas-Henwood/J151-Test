package com.vzap.trytons.resource.fixture;

import com.vzap.trytons.dto.fixture.DeadlineStatusResponseDTO;
import com.vzap.trytons.dto.fixture.LockStatusResponseDTO;
import com.vzap.trytons.service.fixture.DeadlineLockService;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.util.UUID;

@Path("/lock-status")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)

public class LockStatusResource {
    @Inject
    private DeadlineLockService deadlineLockService;

    @GET
    @Path("/{roundId}")
    public Response getLockStatus(@PathParam("roundId") UUID roundId) {
        LockStatusResponseDTO response = deadlineLockService.getLockStatus(roundId);
        return Response.ok(response)
                .build();
    }

    @GET
    @Path("/deadline/{roundId}")
    public Response getDeadlineStatus(@PathParam("roundId") UUID roundId) {
        DeadlineStatusResponseDTO response = deadlineLockService.getDeadlineStatus(roundId);
        return Response.ok(response)
                .build();
    }
}
