package com.vzap.trytons.dao.fixture;

import com.vzap.trytons.enums.FixtureStatus;
import com.vzap.trytons.model.fixture.Fixture;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface FixtureDAO {
    Fixture create(Fixture fixture);
    Optional<Fixture> findById(UUID fixtureId);

    default Optional<Fixture> findFixtureById(UUID fixtureId) {
        return findById(fixtureId);
    }
    List<Fixture> findByLeagueId(UUID leagueId);
    List<Fixture> findByRoundId(UUID roundId);
    List<Fixture> findByTeamId(UUID teamId);
    List<Fixture> findByStatus(FixtureStatus status);
    List<Fixture> getAllFixtures();
    boolean updateFixture(Fixture fixture);
    boolean cancelFixture(UUID fixtureId);
    boolean updateStatus(Fixture fixture, FixtureStatus status);
}
