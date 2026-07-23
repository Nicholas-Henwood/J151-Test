package com.vzap.trytons.service.simulation;

import com.vzap.trytons.dto.simulation.ResimulationRequestDTO;
import com.vzap.trytons.dto.simulation.ResimulationResponseDTO;

import java.util.List;
import java.util.UUID;

public interface ControlledResimulationService {
    ResimulationResponseDTO resimulateFixture(UUID actorUserId, ResimulationRequestDTO request);
    List<ResimulationResponseDTO> listResimulationsForFixture(UUID fixtureId);
}