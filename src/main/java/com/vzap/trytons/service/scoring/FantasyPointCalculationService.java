package com.vzap.trytons.service.scoring;

import com.vzap.trytons.dto.scoring.FantasyPointCalculationResultDTO;

public interface FantasyPointCalculationService {
    FantasyPointCalculationResultDTO calculateForFixture(String fixtureId);
}