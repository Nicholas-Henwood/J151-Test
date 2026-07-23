package com.vzap.trytons.service.results;

import com.vzap.trytons.dto.results.MatchTeamScoreResponseDTO;
import com.vzap.trytons.enums.MatchTeamSide;

import java.util.List;
import java.util.UUID;

public interface MatchTeamScoreService {
    MatchTeamScoreResponseDTO getMatchTeamScoreById(UUID scoreId);
    List<MatchTeamScoreResponseDTO> listMatchTeamScoresForResult(UUID resultId);
    MatchTeamScoreResponseDTO getMatchTeamScoreForResultSide(UUID resultId, MatchTeamSide teamSide);
}