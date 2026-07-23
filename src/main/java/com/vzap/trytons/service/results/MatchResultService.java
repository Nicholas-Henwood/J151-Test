package com.vzap.trytons.service.results;

import com.vzap.trytons.dto.results.MatchResultRequestDTO;
import com.vzap.trytons.dto.results.MatchResultResponseDTO;
import java.util.UUID;

public interface MatchResultService {
    MatchResultResponseDTO captureResult(UUID actorUserId, MatchResultRequestDTO request);
    MatchResultResponseDTO getResult(UUID fixtureId);
}
