package com.vzap.trytons.service.results;

import com.vzap.trytons.dao.fixture.FixtureDAO;
import com.vzap.trytons.dao.results.MatchResultDAO;
import com.vzap.trytons.dao.auth.UserDAO;
import com.vzap.trytons.dao.simulation.SimulationSettingsDAO;
import com.vzap.trytons.model.simulation.SimulationSettings;
import com.vzap.trytons.dto.results.MatchResultRequestDTO;
import com.vzap.trytons.dto.results.MatchResultResponseDTO;
import com.vzap.trytons.enums.FixtureStatus;
import com.vzap.trytons.enums.MatchTeamSide;
import com.vzap.trytons.enums.UserRole;
import com.vzap.trytons.exceptions.AuthorisationException;
import com.vzap.trytons.exceptions.ConflictException;
import com.vzap.trytons.exceptions.DataAccessException;
import com.vzap.trytons.exceptions.ResourceNotFoundException;
import com.vzap.trytons.exceptions.ValidationException;
import com.vzap.trytons.model.fixture.Fixture;
import com.vzap.trytons.model.results.MatchResult;
import com.vzap.trytons.model.auth.User;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.time.LocalDateTime;
import java.util.EnumSet;
import java.util.Set;
import java.util.UUID;

@ApplicationScoped
public class MatchResultServiceImpl implements MatchResultService {
    private static final Set<FixtureStatus> CAPTURABLE_STATES = EnumSet.of(FixtureStatus.LOCKED, FixtureStatus.SIMULATING, FixtureStatus.COMPLETED);
    private final MatchResultDAO matchResultDAO;
    private final FixtureDAO fixtureDAO;
    private final UserDAO userDAO;
    private final SimulationSettingsDAO simulationSettingsDAO;

    @Inject
    public MatchResultServiceImpl(MatchResultDAO matchResultDAO, FixtureDAO fixtureDAO, UserDAO userDAO, SimulationSettingsDAO simulationSettingsDAO) {
        this.matchResultDAO = matchResultDAO;
        this.fixtureDAO = fixtureDAO;
        this.userDAO = userDAO;
        this.simulationSettingsDAO = simulationSettingsDAO;
    }

    @Override
    public MatchResultResponseDTO captureResult(UUID actorUserId, MatchResultRequestDTO request) {
        validateRequest(request);
        requireAdmin(actorUserId);

        Fixture fixture = fixtureDAO.findFixtureById(request.getFixtureId()).orElseThrow(() -> new ResourceNotFoundException("Fixture was not found."));

        if (!CAPTURABLE_STATES.contains(fixture.getStatus())) {
            throw new ConflictException("A match result cannot be captured while the fixture is " + fixture.getStatus() + ".");
        }

        SimulationSettings activeSettings = simulationSettingsDAO.findActive().orElseThrow(() -> new ResourceNotFoundException("simulationSettings"));

        int simulationRunNumber = matchResultDAO.getNextSimulationRunNumber(fixture.getFixtureId());
        matchResultDAO.markAllFixtureResultsNotCurrent(fixture.getFixtureId());
        MatchResult saved = matchResultDAO.save(buildResult(fixture, request, simulationRunNumber, activeSettings.getSettingsId()));

        if (saved == null) {
            throw new DataAccessException("Failed to persist the captured match result.", null);
        }

        return mapToResponse(saved, fixture);
    }

    @Override
    public MatchResultResponseDTO getResult(UUID fixtureId) {
        if (fixtureId == null) {
            throw new ValidationException("Fixture ID is required.");
        }

        MatchResult result = matchResultDAO.findCurrentByFixtureId(fixtureId).orElseThrow(() -> new ResourceNotFoundException("No match result exists for the fixture."));

        Fixture fixture = fixtureDAO.findFixtureById(fixtureId).orElseThrow(() -> new ResourceNotFoundException("Fixture was not found"));

        return mapToResponse(result, fixture);
    }

    private void validateRequest(MatchResultRequestDTO request) {
        if (request == null) {
            throw new ValidationException("Match result details are required.");
        }
        if (request.getFixtureId() == null) {
            throw new ValidationException("Fixture ID is required.");
        }
        if (request.getTeamAScore() < 0 || request.getTeamBScore() < 0) {
            throw new ValidationException("Match scores cannot be negative.");
        }
    }

    private void requireAdmin(UUID actorUserId) {
        if (actorUserId == null) {
            throw new AuthorisationException("An authenticated administrator is required to capture match results.");
        }
        User actor = userDAO.getUserById(actorUserId).orElseThrow(() -> new AuthorisationException("An authenticated administrator is required to capture match results."));
        if (actor.getRole() != UserRole.ADMINISTRATOR) {
            throw new AuthorisationException("Only administrators may capture or correct match results.");
        }
    }

    private MatchResult buildResult(Fixture fixture, MatchResultRequestDTO request, int simulationRunNumber, UUID settingsId) {
        int teamAScore = request.getTeamAScore();
        int teamBScore = request.getTeamBScore();

        return MatchResult.builder()
                .resultId(UUID.randomUUID())
                .fixtureId(fixture.getFixtureId())
                .settingsId(settingsId)
                .simulationRunNumber(simulationRunNumber)
                .teamAScore(teamAScore)
                .teamBScore(teamBScore)
                .winnerSide(resolveWinnerSide(teamAScore, teamBScore))
                .isDraw(teamAScore == teamBScore)
                .approved(false)
                .isCurrent(true)
                .resultDate(LocalDateTime.now())
                .approvedByAdminUserId(null)
                .build();
    }

    private MatchTeamSide resolveWinnerSide(int teamAScore, int teamBScore) {
        if (teamAScore == teamBScore) {
            return null;
        }
        return teamAScore > teamBScore ? MatchTeamSide.TEAM_A : MatchTeamSide.TEAM_B;
    }

    private MatchResultResponseDTO mapToResponse(MatchResult result, Fixture fixture) {
        return new MatchResultResponseDTO(
                result.getResultId(),
                result.getFixtureId(),
                fixture.getTeamAId(),
                fixture.getTeamBId(),
                result.getSimulationRunNumber(),
                result.getTeamAScore(),
                result.getTeamBScore(),
                result.getWinnerSide(),
                result.isDraw(),
                result.isApproved(),
                result.isCurrent(),
                result.getResultDate(),
                result.getApprovedByAdminUserId()
        );
    }
}
