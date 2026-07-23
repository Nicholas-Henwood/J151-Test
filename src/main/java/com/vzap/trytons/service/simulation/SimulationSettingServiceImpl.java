package com.vzap.trytons.service.simulation;

import com.vzap.trytons.dao.simulation.SimulationSettingsDAO;
import com.vzap.trytons.dao.auth.UserDAO;
import com.vzap.trytons.dto.simulation.SimulationSettingRequestDTO;
import com.vzap.trytons.dto.simulation.SimulationSettingResponseDTO;
import com.vzap.trytons.enums.UserRole;
import com.vzap.trytons.exceptions.AuthorisationException;
import com.vzap.trytons.exceptions.ConflictException;
import com.vzap.trytons.exceptions.ResourceNotFoundException;
import com.vzap.trytons.exceptions.ValidationException;
import com.vzap.trytons.model.simulation.SimulationSettings;
import com.vzap.trytons.model.auth.User;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@ApplicationScoped
public class SimulationSettingServiceImpl implements SimulationSettingService {
    @Inject
    private SimulationSettingsDAO simulationSettingsDAO;

    @Inject
    private UserDAO userDAO;

    @Override
    public SimulationSettingResponseDTO createSimulationSetting(UUID actorUserId, SimulationSettingRequestDTO request) {

        validateAdministrator(actorUserId);
        validateRequest(request);
        validateUniqueSeason(request.getSeason(),null);
        SimulationSettings settings = mapRequestToSettings(request);
        if (Boolean.TRUE.equals(settings.getIsActive())) {
            simulationSettingsDAO.markAllSettingsInactive();
        }
        return mapToResponse(simulationSettingsDAO.save(settings));
    }

    @Override
    public SimulationSettingResponseDTO updateSimulationSetting(UUID actorUserId, UUID simulationSettingsId, SimulationSettingRequestDTO request) {

        if (simulationSettingsId == null) {
            throw new ValidationException("simulationSettingsId is null");
        }

        validateAdministrator(actorUserId);
        validateRequest(request);
        SimulationSettings settings = simulationSettingsDAO.findById(simulationSettingsId).orElseThrow(()-> new ResourceNotFoundException("simulationSettings"));
        validateUniqueSeason(request.getSeason(),simulationSettingsId);
        SimulationSettings updatedSettings = updateSettings(settings, request);


        if (Boolean.TRUE.equals(settings.getIsActive())) {
            simulationSettingsDAO.markAllSettingsInactive();
        }

        return mapToResponse(simulationSettingsDAO.save(updatedSettings));
    }

    @Override
    public SimulationSettingResponseDTO getSimulationSettingById(UUID simulationSettingsId){
        if (simulationSettingsId == null) {
            throw new ValidationException("simulationSettingsId is null");
        }
        SimulationSettings settings = simulationSettingsDAO.findById(simulationSettingsId).orElseThrow(()-> new ResourceNotFoundException("simulationSettings"));
        return mapToResponse(settings);
    }

    @Override
    public SimulationSettingResponseDTO getActiveSimulationSetting() {
        SimulationSettings settings = simulationSettingsDAO.findActive().orElseThrow(()-> new ResourceNotFoundException("simulationSettings"));
        return mapToResponse(settings);
    }

    @Override
    public List<SimulationSettingResponseDTO> listSimulationSettings() {
        List<SimulationSettings> settings = simulationSettingsDAO.findAll();
        List<SimulationSettingResponseDTO> responseSettings = new ArrayList<>();
        for(SimulationSettings simulationSetting : settings) {
            responseSettings.add(mapToResponse(simulationSetting));
        }
        return responseSettings;
    }

    private SimulationSettings updateSettings(SimulationSettings settings, SimulationSettingRequestDTO request) {
        settings.setSeason(request.getSeason());
        settings.setPlayerAbilityWeight(request.getPlayerAbilityWeight());
        settings.setPlayerFormWeight(request.getPlayerFormWeight());
        settings.setTeamBalanceWeight(request.getTeamBalanceWeight());
        settings.setRandomVariationWeight(request.getRandomVariationWeight());

        settings.setRequireAdminApproval(request.getRequireAdminApproval());
        settings.setAllowResimulation(request.getAllowResimulation());
        settings.setMaxResimulations(request.getMaxResimulations());

        settings.setIsActive(request.getIsActive());
        return settings;
    }
    private void validateAdministrator(UUID actorUserId) {

        if (actorUserId == null) {
            throw new AuthorisationException("An authenticated administrator ID is required.");
        }

        User user = userDAO.getUserById(actorUserId).orElseThrow(() -> new AuthorisationException("The authenticated administrator could not be found."));

        if (!Boolean.TRUE.equals(user.getIsActive())) {
            throw new AuthorisationException("The authenticated administrator account is inactive.");
        }

        if (user.getRole() != UserRole.ADMINISTRATOR) {
            throw new AuthorisationException("Administrator access is required.");
        }
    }

    private void validateRequest(SimulationSettingRequestDTO request) {

        if (request == null) {
            throw new ValidationException("Simulation settings are required.");
        }

        if (request.getSeason() == null || request.getSeason().trim().isEmpty()) {
            throw new ValidationException("A season is required.");
        }

        if (request.getSeason().trim().length() > 20) {
            throw new ValidationException("The season may not exceed 20 characters.");
        }

        if (request.getPlayerAbilityWeight() == null || request.getPlayerFormWeight() == null || request.getTeamBalanceWeight() == null || request.getRandomVariationWeight() == null) {
            throw new ValidationException("All simulation weights are required.");
        }

        validateWeight(request.getPlayerAbilityWeight());
        validateWeight(request.getPlayerFormWeight());
        validateWeight(request.getTeamBalanceWeight());
        validateWeight(request.getRandomVariationWeight());

        BigDecimal totalWeight = request.getPlayerAbilityWeight()
                        .add(request.getPlayerFormWeight())
                        .add(request.getTeamBalanceWeight())
                        .add(request.getRandomVariationWeight());

        if (totalWeight.compareTo(new BigDecimal("100.00")) != 0) {
            throw new ValidationException("Simulation weights must total 100.");
        }

        if (request.getRequireAdminApproval() == null || request.getAllowResimulation() == null || request.getIsActive() == null) {
            throw new ValidationException("All simulation setting options are required.");
        }

        if (request.getMaxResimulations() < 0) {
            throw new ValidationException("Maximum resimulations may not be negative.");
        }

        if (!request.getAllowResimulation() && request.getMaxResimulations() != 0) {
            throw new ValidationException("Maximum resimulations must be zero when resimulation is disabled.");
        }

        if (request.getAllowResimulation() && request.getMaxResimulations() == 0) {
            throw new ValidationException("Maximum resimulations must be greater than zero when resimulation is enabled.");
        }
    }

    private void validateWeight(BigDecimal weight) {

        if (weight.compareTo(BigDecimal.ZERO) < 0) {
            throw new ValidationException("Simulation weights may not be negative.");
        }

        if (weight.compareTo(new BigDecimal("100.00")) > 0) {
            throw new ValidationException("A simulation weight may not exceed 100.");
        }
    }

    private SimulationSettings mapRequestToSettings(SimulationSettingRequestDTO request) {

        return SimulationSettings.builder()
                .season(request.getSeason())
                .playerAbilityWeight(request.getPlayerAbilityWeight())
                .playerFormWeight(request.getPlayerFormWeight())
                .teamBalanceWeight(request.getTeamBalanceWeight())
                .randomVariationWeight(request.getRandomVariationWeight())
                .requireAdminApproval(request.getRequireAdminApproval())
                .allowResimulation(request.getAllowResimulation())
                .maxResimulations(request.getMaxResimulations())
                .isActive(request.getIsActive())
                .build();
    }

    private SimulationSettingResponseDTO mapToResponse(SimulationSettings settings) {

        return SimulationSettingResponseDTO.builder()
                .settingsId(settings.getSettingsId())
                .season(settings.getSeason())
                .playerAbilityWeight(settings.getPlayerAbilityWeight())
                .playerFormWeight(settings.getPlayerFormWeight())
                .teamBalanceWeight(settings.getTeamBalanceWeight())
                .randomVariationWeight(settings.getRandomVariationWeight())
                .requireAdminApproval(settings.getRequireAdminApproval())
                .allowResimulation(settings.getAllowResimulation())
                .maxResimulations(settings.getMaxResimulations())
                .isActive(settings.getIsActive())
                .createdAt(settings.getCreatedAt())
                .updatedAt(settings.getUpdatedAt())
                .build();
    }

    private void validateUniqueSeason(String season, UUID currentSettingsId) {

        List<SimulationSettings> existingSettings = simulationSettingsDAO.findAll();

        for (SimulationSettings settings : existingSettings) {
            boolean sameSeason = false;
            boolean differentRecord = false;

            if (settings.getSeason() != null && settings.getSeason().equalsIgnoreCase(season)) {
                sameSeason = true;
            }

            if (currentSettingsId == null) {
                differentRecord = true;
            } else if (!currentSettingsId.equals(settings.getSettingsId())) {
                differentRecord = true;
            }

            if (sameSeason && differentRecord) {
                throw new ConflictException("Simulation settings already exist for this season.");
            }
        }
    }
}
