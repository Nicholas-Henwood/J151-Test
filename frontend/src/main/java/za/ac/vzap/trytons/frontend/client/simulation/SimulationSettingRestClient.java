package za.ac.vzap.trytons.frontend.client.simulation;

import jakarta.enterprise.context.Dependent;
import jakarta.inject.Inject;
import za.ac.vzap.trytons.frontend.client.shared.APIClient;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

@Dependent
public class SimulationSettingRestClient {

    private static final String SIMULATION_SETTINGS_PATH = "/simulation-settings";
    private static final String ACTIVE_PATH = "/simulation-settings/active";

    private static final Logger LOG = Logger.getLogger(SimulationSettingRestClient.class.getName());

    @Inject
    private APIClient apiClient;

    public Optional<SimulationSettingResponse> createSimulationSetting(SimulationSettingRequest request) {
        if (request == null) {
            LOG.log(Level.WARNING, "Simulation setting request is required to create simulation settings.");
            return Optional.empty();
        }

        Optional<SimulationSettingResponse> response = apiClient.post(SIMULATION_SETTINGS_PATH, request, SimulationSettingResponse.class);
        if (response.isEmpty()) {
            LOG.log(Level.WARNING, "Unable to create simulation settings.");
        }
        return response;
    }

    public Optional<SimulationSettingResponse> updateSimulationSetting(UUID simulationSettingsId, SimulationSettingRequest request) {
        if (simulationSettingsId == null || request == null) {
            LOG.log(Level.WARNING, "Simulation settings id and request are required to update simulation settings.");
            return Optional.empty();
        }

        String path = SIMULATION_SETTINGS_PATH + "/" + encode(simulationSettingsId.toString());
        Optional<SimulationSettingResponse> response = apiClient.put(path, request, SimulationSettingResponse.class);
        if (response.isEmpty()) {
            LOG.log(Level.WARNING, "Unable to update simulation settings.");
        }
        return response;
    }

    public Optional<SimulationSettingResponse> getSimulationSettingById(UUID simulationSettingsId) {
        if (simulationSettingsId == null) {
            LOG.log(Level.WARNING, "Simulation settings id is required to get simulation settings.");
            return Optional.empty();
        }

        String path = SIMULATION_SETTINGS_PATH + "/" + encode(simulationSettingsId.toString());
        Optional<SimulationSettingResponse> response = apiClient.get(path, SimulationSettingResponse.class);
        if (response.isEmpty()) {
            LOG.log(Level.WARNING, "Unable to get simulation settings.");
        }
        return response;
    }

    public Optional<SimulationSettingResponse> getActiveSimulationSetting() {
        Optional<SimulationSettingResponse> response = apiClient.get(ACTIVE_PATH, SimulationSettingResponse.class);
        if (response.isEmpty()) {
            LOG.log(Level.WARNING, "Unable to get the active simulation settings.");
        }
        return response;
    }

    public Optional<List<SimulationSettingResponse>> listSimulationSettings() {
        Optional<SimulationSettingResponse[]> response = apiClient.get(SIMULATION_SETTINGS_PATH, SimulationSettingResponse[].class);
        if (response.isEmpty()) {
            LOG.log(Level.WARNING, "Unable to list simulation settings.");
        }
        return response.map(settings -> new ArrayList<>(Arrays.asList(settings)));
    }

    private String encode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }
}
