package com.vzap.trytons.service.scoring;

import com.vzap.trytons.dao.scoring.FantasyPointBreakdownDAO;
import com.vzap.trytons.dao.scoring.FantasyPointsDAO;
import com.vzap.trytons.dao.fixture.FantasyRoundDAO;
import com.vzap.trytons.dao.fixture.FixtureDAO;
import com.vzap.trytons.dao.results.MatchResultDAO;
import com.vzap.trytons.dao.results.PlayerStatisticsDAO;
import com.vzap.trytons.dao.scoring.ScoringRuleDAO;
import com.vzap.trytons.dto.scoring.FantasyPointCalculationResultDTO;
import com.vzap.trytons.exceptions.BusinessRuleException;
import com.vzap.trytons.exceptions.ResourceNotFoundException;
import com.vzap.trytons.model.scoring.FantasyPointBreakdown;
import com.vzap.trytons.model.scoring.FantasyPoints;
import com.vzap.trytons.model.fixture.FantasyRound;
import com.vzap.trytons.model.fixture.Fixture;
import com.vzap.trytons.model.results.MatchResult;
import com.vzap.trytons.model.results.PlayerStatistics;
import com.vzap.trytons.model.scoring.ScoringRule;
import com.vzap.trytons.util.ScoringCalculator;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@ApplicationScoped
public class FantasyPointCalculationServiceImpl implements FantasyPointCalculationService {
    @Inject
    FixtureDAO fixtureDAO;

    @Inject
    PlayerStatisticsDAO playerStatisticsDAO;

    @Inject
    ScoringRuleDAO scoringRuleDAO;

    @Inject
    MatchResultDAO matchResultDAO;

    @Inject
    FantasyPointsDAO fantasyPointsDAO;

    @Inject
    FantasyPointBreakdownDAO fantasyPointBreakdownDAO;

    @Inject
    FantasyRoundDAO fantasyRoundDAO;

    @Override
    public FantasyPointCalculationResultDTO calculateForFixture(String fixtureId) {
        UUID currentFixtureId = UUID.fromString(fixtureId);

        MatchResult currentResult = matchResultDAO.findCurrentByFixtureId(currentFixtureId).orElseThrow(() -> new ResourceNotFoundException("No current result exists for this fixture"));

        List<PlayerStatistics> playerStatistics = playerStatisticsDAO.findByResultId(currentResult.getResultId());

        if (playerStatistics.isEmpty()){
            throw new BusinessRuleException("no player statistics were found");
        }

        Fixture currentFixture = fixtureDAO.findById(currentFixtureId)
                .orElseThrow(() -> new ResourceNotFoundException("Fixture was not found"));

        FantasyRound round = fantasyRoundDAO.getRoundById(currentFixture.getRoundId()).orElseThrow(() -> new ResourceNotFoundException("Round was not found"));
        String season = round.getSeason();

        List<ScoringRule> scoringRules = scoringRuleDAO.findActiveRules(season);

        if (scoringRules.isEmpty()){
            throw new BusinessRuleException("no scoring rules were found");
        }

        int pointsRowsWritten = 0;
        int finalCalculationVersion = 1;

        for (PlayerStatistics statistic : playerStatistics) {

            ScoringCalculator.Result scoring = ScoringCalculator.calculate(statistic, scoringRules);
            int total = scoring.totalPoints();
            List<FantasyPointBreakdown> pointBreakdowns = scoring.breakdowns();


                UUID statId = statistic.getStatId();

                int nextVersion = fantasyPointsDAO.getNextCalculationVersion(statId);

                fantasyPointsDAO.markExistingPointsForStatAsNotFinal(statId);

                FantasyPoints savedFantasyPoints = fantasyPointsDAO.save(
                        FantasyPoints.builder()
                                .statId(statId)
                                .totalPoints(total)
                                .calculationVersion(nextVersion)
                                .isFinal(true)
                                .calculatedAt(LocalDateTime.now())
                                .build()
                );

            for (FantasyPointBreakdown breakdown : pointBreakdowns) {
                breakdown.setPointsId(savedFantasyPoints.getPointsId());
                fantasyPointBreakdownDAO.save(breakdown);
            }

            pointsRowsWritten++;
            finalCalculationVersion = savedFantasyPoints.getCalculationVersion();
            }

        FantasyPointCalculationResultDTO fantasyPointCalculationResultDTO = FantasyPointCalculationResultDTO.builder()
                .fixtureId(currentFixtureId.toString())
                .pointsRowsWritten(pointsRowsWritten)
                .calculationVersion(finalCalculationVersion)
                .build();

        return fantasyPointCalculationResultDTO;

    }
}
