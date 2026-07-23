package com.vzap.trytons.service.scoring;

import com.vzap.trytons.dto.scoring.ScoringRuleRequestDTO;
import com.vzap.trytons.dto.scoring.ScoringRuleResponseDTO;

import java.util.List;
import java.util.UUID;

public interface ScoringRuleService {

    List<ScoringRuleResponseDTO>listRules(UUID actorUserId, String season);

    ScoringRuleResponseDTO saveRule(
            UUID actorUserId,
            ScoringRuleRequestDTO request);
}
