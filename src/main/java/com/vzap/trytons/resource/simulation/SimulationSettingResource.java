package com.vzap.trytons.resource.simulation;

import com.vzap.trytons.annotations.AdminOnly;
import com.vzap.trytons.annotations.Authenticated;
import com.vzap.trytons.dto.simulation.SimulationSettingRequestDTO;
import com.vzap.trytons.dto.simulation.SimulationSettingResponseDTO;
import com.vzap.trytons.exceptions.AuthenticationException;
import com.vzap.trytons.filter.AuthFilter;
import com.vzap.trytons.security.AuthPrincipal;
import com.vzap.trytons.service.simulation.SimulationSettingService;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.util.List;
import java.util.UUID;

@RequestScoped
@Authenticated
@AdminOnly
@Path("/simulation-settings")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class SimulationSettingResource {
    @Inject
    private SimulationSettingService simulationSettingService;

    @Context
    private ContainerRequestContext requestContext;

    @POST
    public Response createSimulationSetting(SimulationSettingRequestDTO request) {
        AuthPrincipal principal = (AuthPrincipal) requestContext.getProperty(AuthFilter.CURRENT_USER_PROPERTY);
        if (principal == null) {
            throw new AuthenticationException("The authenticated user could not be identified.");
        }

        SimulationSettingResponseDTO response = simulationSettingService.createSimulationSetting(principal.getUserId(), request);


        return Response.status(Response.Status.CREATED)
                .entity(response)
                .build();
    }

    @PUT
    @Path("/{simulationSettingsId}")
    public Response updateSimulationSetting(@PathParam("simulationSettingsId") UUID simulationSettingsId, SimulationSettingRequestDTO request) {
        AuthPrincipal principal = (AuthPrincipal) requestContext.getProperty(AuthFilter.CURRENT_USER_PROPERTY);

        if (principal == null) {
            throw new AuthenticationException("The authenticated user could not be identified.");
        }

        SimulationSettingResponseDTO response =  simulationSettingService.updateSimulationSetting(principal.getUserId(), simulationSettingsId, request);


        return Response.status(Response.Status.OK)
                .entity(response)
                .build();
    }

    @GET
    @Path("/{simulationSettingsId}")
    public Response getSimulationSettingById(@PathParam("simulationSettingsId") UUID simulationSettingsId) {
        SimulationSettingResponseDTO response = simulationSettingService.getSimulationSettingById(simulationSettingsId);

        return Response.status(Response.Status.OK)
                .entity(response)
                .build();
    }

    @GET
    @Path("/active")
    public Response getActiveSimulationSetting() {
        SimulationSettingResponseDTO response = simulationSettingService.getActiveSimulationSetting();

        return Response.status(Response.Status.OK)
                .entity(response)
                .build();
    }

    @GET
    public Response listSimulationSettings() {
        List<SimulationSettingResponseDTO> responseList = simulationSettingService.listSimulationSettings();

        return  Response.status(Response.Status.OK)
                .entity(responseList)
                .build();
    }
}
