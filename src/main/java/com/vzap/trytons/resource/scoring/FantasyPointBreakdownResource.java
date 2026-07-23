package com.vzap.trytons.resource.scoring;

import com.vzap.trytons.dto.scoring.FantasyPointBreakdownResponseDTO;
import com.vzap.trytons.service.scoring.FantasyPointBreakdownService;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.util.List;
import java.util.UUID;

@RequestScoped
@Path("/fantasy-point-breakdowns")
@Produces(MediaType.APPLICATION_JSON)
public class FantasyPointBreakdownResource {
    @Inject
    FantasyPointBreakdownService fantasyPointBreakdownService;

    @GET
    @Path("/{breakdownId}")
    public Response getBreakdownById(@PathParam("breakdownId") UUID breakdownId) {
        FantasyPointBreakdownResponseDTO result = fantasyPointBreakdownService.getBreakdownById(breakdownId);

        return Response.ok(result)
                .build();
    }

    @GET
    @Path("/points/{pointsId}")
    public Response listBreakdownsForPoints(@PathParam("pointsId") UUID pointsId) {
        List<FantasyPointBreakdownResponseDTO> results = fantasyPointBreakdownService.listBreakdownsForPoints(pointsId);

        return Response.ok(results)
                .build();
    }
}
