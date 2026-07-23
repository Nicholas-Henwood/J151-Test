package com.vzap.trytons.service.fixture;

import com.vzap.trytons.dto.fixture.RoundResponseDTO;
import com.vzap.trytons.enums.FantasyRoundStatus;

import java.util.List;
import java.util.Optional;

public interface RoundService {
    List<RoundResponseDTO> listRounds();
    List<RoundResponseDTO> listRoundsByStatus(FantasyRoundStatus status);
    Optional<RoundResponseDTO> getCurrentOpenRound();
}
