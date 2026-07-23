package com.vzap.trytons.service.results;

import com.vzap.trytons.dao.fantasyteam.FantasyTeamRoundSelectionDAO;
import com.vzap.trytons.dao.fixture.FixtureDAO;
import com.vzap.trytons.dao.results.MatchResultDAO;
import com.vzap.trytons.dao.results.PlayerStatisticsDAO;
import com.vzap.trytons.dao.auth.UserDAO;
import com.vzap.trytons.dto.results.PlayerStatisticsRequestDTO;
import com.vzap.trytons.dto.results.PlayerStatisticsResponseDTO;
import com.vzap.trytons.enums.UserRole;
import com.vzap.trytons.exceptions.*;
import com.vzap.trytons.model.fixture.Fixture;
import com.vzap.trytons.model.results.MatchResult;
import com.vzap.trytons.model.results.PlayerStatistics;
import com.vzap.trytons.model.auth.User;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@ApplicationScoped
public class PlayerStatisticsServiceImpl implements PlayerStatisticsService {
    @Inject
    private PlayerStatisticsDAO playerStatisticsDAO;

    @Inject
    private MatchResultDAO matchResultDAO;

    @Inject
    private FixtureDAO fixtureDAO;

    @Inject
    private FantasyTeamRoundSelectionDAO roundSelectionDAO;

    @Inject
    private UserDAO userDAO;


    @Override
    public List<PlayerStatisticsResponseDTO> listResultStatistics(UUID resultId) {
        MatchResult result = requireResult(resultId);
        return playerStatisticsDAO.findByResultId(result.getResultId()).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<PlayerStatisticsResponseDTO> listResultStatisticsForTeam(UUID resultId, UUID teamId) {
        MatchResult result = requireResult(resultId);
        if (teamId == null) {
            throw new ValidationException("Team ID is required.");
        }

        Fixture fixture = fixtureDAO.findFixtureById(result.getFixtureId()).orElseThrow(()-> new ResourceNotFoundException("Fixture was not found for the result."));

        requireTeamInFixture(fixture, teamId);
        return playerStatisticsDAO.findByResultIdAndTeamId(result.getResultId(), teamId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public PlayerStatisticsResponseDTO captureStatistic(UUID actorUserId, PlayerStatisticsRequestDTO request) {
        validateRequest(request);
        requireAdmin(actorUserId);
        MatchResult result = requireResult(request.getResultId());

        if (!result.isCurrent()) {
            throw new BusinessRuleException("Statistics cannot be captured for a historical match result.");
        }

        if (result.isApproved()) {
            throw new BusinessRuleException("Statistics cannot be captured after the match result has been approved.");
        }

        Fixture fixture = fixtureDAO.findFixtureById(result.getFixtureId()).orElseThrow(() -> new ResourceNotFoundException("Fixture was not found for the result."));
        requireTeamInFixture(fixture, request.getTeamId());
        if (fixture.getRoundId() == null) {
            throw new BusinessRuleException("The fixture is not linked to a fantasy round.");
        }
        requirePlayerInLockedSquad(fixture.getRoundId(), request.getTeamId(), request.getPlayerId());

        if (playerStatisticsDAO.findByResultIdAndTeamIdAndPlayerId(request.getResultId(), request.getTeamId(), request.getPlayerId()).isPresent()) {
            throw new ConflictException("Statistics have already been captured for this player in the result.");
        }

        PlayerStatistics saved = playerStatisticsDAO.save(buildStatistics(request)).orElseThrow(() -> new DataAccessException("Failed to persist the captured player statistics.", null));

        return mapToResponse(saved);
    }

    private MatchResult requireResult(UUID resultId) {
        if (resultId == null) {
            throw new ValidationException("Result ID is required.");
        }
        return matchResultDAO.findById(resultId).orElseThrow(() -> new ResourceNotFoundException("Match result was not found."));
    }

    private void validateRequest(PlayerStatisticsRequestDTO request) {
        if (request == null) {
            throw new ValidationException("Player statistics details are required.");
        }
        if (request.getResultId() == null) {
            throw new ValidationException("Result ID is required.");
        }
        if (request.getTeamId() == null) {
            throw new ValidationException("Team ID is required.");
        }
        if (request.getPlayerId() == null) {
            throw new ValidationException("Player ID is required.");
        }
        if (request.getTries() < 0 || request.getAssists() < 0 || request.getTackles() < 0 || request.getMissedTackles() < 0 || request.getConversions() < 0 || request.getPenalties() < 0 || request.getMetersGained() < 0 || request.getYellowCards() < 0 || request.getRedCards() < 0) {
            throw new ValidationException("Player statistics values cannot be negative.");
        }
    }

    private void requireAdmin(UUID actorUserId) {
        if (actorUserId == null) {
            throw new AuthenticationException("An authenticated administrator is required to capture player statistics.");
        }

        User actor = userDAO.getUserById(actorUserId).orElseThrow(() -> new AuthenticationException("An authenticated administrator is required to capture player statistics."));

        if (actor.getRole() != UserRole.ADMINISTRATOR) {
            throw new AuthorisationException("Only administrators may capture or correct player statistics.");
        }
    }

    private void requireTeamInFixture(Fixture fixture, UUID teamId) {
        UUID teamAId = fixture.getTeamAId();
        UUID teamBId = fixture.getTeamBId();
        if (!teamId.equals(teamAId) && !teamId.equals(teamBId)) {
            throw new ResourceNotFoundException("The team does not belong to the result's fixture.");
        }
    }

    private void requirePlayerInLockedSquad(UUID roundId, UUID teamId, UUID playerId) {
        boolean present = roundSelectionDAO
                .getSelectionsByRoundIdAndTeamId(roundId, teamId)
                .stream()
                .anyMatch(selection -> playerId.equals(selection.getPlayerId()) && selection.getLockedAt() != null);
        if (!present) {
            throw new BusinessRuleException("The player is not part of the team's locked round squad.");
        }
    }

    private PlayerStatistics buildStatistics(PlayerStatisticsRequestDTO request) {
        return PlayerStatistics.builder()
                .statId(UUID.randomUUID())
                .resultId(request.getResultId())
                .teamId(request.getTeamId())
                .playerId(request.getPlayerId())
                .tries(request.getTries())
                .assists(request.getAssists())
                .tackles(request.getTackles())
                .missedTackles(request.getMissedTackles())
                .conversions(request.getConversions())
                .penalties(request.getPenalties())
                .metersGained(request.getMetersGained())
                .yellowCards(request.getYellowCards())
                .redCards(request.getRedCards())
                .statisticDate(LocalDateTime.now())
                .build();
    }

    private PlayerStatisticsResponseDTO mapToResponse(PlayerStatistics stat) {
        return new PlayerStatisticsResponseDTO(stat.getStatId(), stat.getResultId(), stat.getTeamId(), stat.getPlayerId(), stat.getTries(), stat.getAssists(), stat.getTackles(), stat.getMissedTackles(), stat.getConversions(), stat.getPenalties(), stat.getMetersGained(), stat.getYellowCards(), stat.getRedCards(), stat.getStatisticDate());
    }
}
