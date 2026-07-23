package com.vzap.trytons.dao.simulation;

import com.vzap.trytons.model.simulation.SimulationSettings;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface SimulationSettingsDAO {
    SimulationSettings save(SimulationSettings simulationSettings);
    Optional<SimulationSettings> findById(UUID settingsId);
    Optional<SimulationSettings> findActive();
    List<SimulationSettings> findAll();
    int markAllSettingsInactive();
}