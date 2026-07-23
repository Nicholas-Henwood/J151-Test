package com.vzap.trytons.service.scoring;

import com.vzap.trytons.dao.scoring.FantasyPointBreakdownDAO;
import com.vzap.trytons.dao.scoring.FantasyPointsDAO;
import com.vzap.trytons.dao.fixture.FantasyRoundDAO;
import com.vzap.trytons.dao.fixture.FixtureDAO;
import com.vzap.trytons.dao.results.MatchResultDAO;
import com.vzap.trytons.dao.results.PlayerStatisticsDAO;
import com.vzap.trytons.dao.scoring.ScoringRuleDAO;
import com.vzap.trytons.dao.auth.UserDAO;
import com.vzap.trytons.dto.scoring.FantasyPointsRequestDTO;
import com.vzap.trytons.dto.scoring.FantasyPointsResponseDTO;
import com.vzap.trytons.enums.UserRole;
import com.vzap.trytons.exceptions.AuthorisationException;
import com.vzap.trytons.exceptions.BusinessRuleException;
import com.vzap.trytons.exceptions.ResourceNotFoundException;
import com.vzap.trytons.model.scoring.FantasyPointBreakdown;
import com.vzap.trytons.model.scoring.FantasyPoints;
import com.vzap.trytons.model.fixture.FantasyRound;
import com.vzap.trytons.model.fixture.Fixture;
import com.vzap.trytons.model.results.MatchResult;
import com.vzap.trytons.model.results.PlayerStatistics;
import com.vzap.trytons.model.scoring.ScoringRule;
import com.vzap.trytons.model.auth.User;
import com.vzap.trytons.util.ScoringCalculator;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@ApplicationScoped
public class FantasyPointsServiceImpl implements FantasyPointsService {
    @Inject
    FantasyPointsDAO fantasyPointsDAO;

    @Inject
    PlayerStatisticsDAO playerStatisticsDAO;

    @Inject
    MatchResultDAO matchResultDAO;

    @Inject
    FixtureDAO fixtureDAO;

    @Inject
    FantasyRoundDAO fantasyRoundDAO;

    @Inject
    ScoringRuleDAO scoringRuleDAO;

    @Inject
    FantasyPointBreakdownDAO fantasyPointBreakdownDAO;

    @Inject
    UserDAO userDAO;

    @Override
    public FantasyPointsResponseDTO calculateFantasyPoints(UUID actorUserId, FantasyPointsRequestDTO request) {
        requireAdmin(actorUserId);

        PlayerStatistics statistic = playerStatisticsDAO.findById(request.getStatId()).orElseThrow(() -> new ResourceNotFoundException("Player statistic not found."));

        MatchResult result = matchResultDAO.findById(statistic.getResultId()).orElseThrow(() -> new ResourceNotFoundException("Match result not found."));

        Fixture fixture = fixtureDAO.findById(result.getFixtureId()).orElseThrow(() -> new ResourceNotFoundException("Fixture not found."));

        FantasyRound round = fantasyRoundDAO.getRoundById(fixture.getRoundId()).orElseThrow(() -> new ResourceNotFoundException("Round was not found."));

        List<ScoringRule> scoringRules = scoringRuleDAO.findActiveRules(round.getSeason());

        if (scoringRules.isEmpty()) {
            throw new BusinessRuleException("no scoring rules were found");
        }

        ScoringCalculator.Result scoring = ScoringCalculator.calculate(statistic, scoringRules);

        int total = scoring.totalPoints();
        
        List<FantasyPointBreakdown> pointBreakdowns = scoring.breakdowns();

        UUID statId = statistic.getStatId();
        int nextVersion = fantasyPointsDAO.getNextCalculationVersion(statId);
        fantasyPointsDAO.markExistingPointsForStatAsNotFinal(statId);

        FantasyPoints savedPoints = fantasyPointsDAO.save(
                FantasyPoints.builder()
                        .statId(statId)
                        .totalPoints(total)
                        .calculationVersion(nextVersion)
                        .isFinal(true)
                        .calculatedAt(LocalDateTime.now())
                        .build()
        );

        for (FantasyPointBreakdown breakdown : pointBreakdowns) {
            breakdown.setPointsId(savedPoints.getPointsId());
            fantasyPointBreakdownDAO.save(breakdown);
        }

        return mapToResponse(savedPoints);
    }

    @Override
    public FantasyPointsResponseDTO getFantasyPointsById(UUID pointsId) {
        FantasyPoints points = fantasyPointsDAO.findById(pointsId)
                .orElseThrow(() -> new ResourceNotFoundException("Fantasy points not found."));
        return mapToResponse(points);
    }

    @Override
    public List<FantasyPointsResponseDTO> listFantasyPointsForStat(UUID statId) {
        List<FantasyPointsResponseDTO> responses = new ArrayList<>();
        for (FantasyPoints points : fantasyPointsDAO.findByStatId(statId)) {
            responses.add(mapToResponse(points));
        }
        return responses;
    }

    @Override
    public FantasyPointsResponseDTO getFinalFantasyPointsForStat(UUID statId) {
        FantasyPoints points = fantasyPointsDAO.findFinalByStatId(statId)
                .orElseThrow(() -> new ResourceNotFoundException("No final fantasy points exist for this statistic."));
        return mapToResponse(points);
    }

    private void requireAdmin(UUID actorUserId) {
        if (actorUserId == null) {
            throw new AuthorisationException("An authenticated administrator is required.");
        }
        User actor = userDAO.getUserById(actorUserId)
                .orElseThrow(() -> new AuthorisationException("An authenticated administrator is required."));
        if (actor.getRole() != UserRole.ADMINISTRATOR) {
            throw new AuthorisationException("Only administrators may trigger fantasy point calculation.");
        }
    }

    private FantasyPointsResponseDTO mapToResponse(FantasyPoints points) {
        return new FantasyPointsResponseDTO(
                points.getPointsId(),
                points.getStatId(),
                points.getTotalPoints(),
                points.getCalculationVersion(),
                points.isFinal(),
                points.getCalculatedAt()
        );
    }

}