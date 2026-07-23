package com.vzap.trytons.service.simulation;

import com.vzap.trytons.dto.simulation.MatchProcessingResultDTO;

import java.util.UUID;

public interface MatchProcessingService {
    MatchProcessingResultDTO processCompletedFixture(UUID actorUserId, UUID fixtureId);
}
