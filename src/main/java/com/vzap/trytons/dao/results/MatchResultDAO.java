package com.vzap.trytons.dao.results;

import com.vzap.trytons.model.results.MatchResult;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface MatchResultDAO {

    MatchResult save(MatchResult matchResult);
    Optional<MatchResult> findById(UUID resultId);
    Optional<MatchResult> findCurrentByFixtureId(UUID fixtureId);
    List<MatchResult> findAllByFixtureId(UUID fixtureId);
    int getNextSimulationRunNumber(UUID fixtureId);
    int markAllFixtureResultsNotCurrent(UUID fixtureId);
}
