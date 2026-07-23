package com.vzap.trytons.service.simulation;

import com.vzap.trytons.dto.simulation.CompetitionProcessingSummaryDTO;

import java.util.UUID;

public interface CompetitionProcessingService {
    CompetitionProcessingSummaryDTO processDueWork(UUID actorUserId);
}