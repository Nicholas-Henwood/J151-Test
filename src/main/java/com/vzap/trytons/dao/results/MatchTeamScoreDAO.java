package com.vzap.trytons.dao.results;

import com.vzap.trytons.enums.MatchTeamSide;
import com.vzap.trytons.model.results.MatchTeamScore;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface MatchTeamScoreDAO {
    MatchTeamScore save(MatchTeamScore matchTeamScore);
    MatchTeamScore update(MatchTeamScore matchTeamScore);
    Optional<MatchTeamScore> findById(UUID scoreId);
    List<MatchTeamScore> findByResultId(UUID resultId);
    Optional<MatchTeamScore> findByResultIdAndTeamSide(UUID resultId, MatchTeamSide teamSide);
}