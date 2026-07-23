package com.vzap.trytons.service.simulation;

import com.vzap.trytons.dto.results.MatchResultResponseDTO;

import java.util.UUID;

public interface MatchSimulationService {
    MatchResultResponseDTO simulateFixture(UUID actorUserId, UUID fixtureId);
}