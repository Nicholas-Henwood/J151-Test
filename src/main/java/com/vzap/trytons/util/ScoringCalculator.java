package com.vzap.trytons.util;

import com.vzap.trytons.model.scoring.FantasyPointBreakdown;
import com.vzap.trytons.model.results.PlayerStatistics;
import com.vzap.trytons.model.scoring.ScoringRule;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public final class ScoringCalculator {
    private ScoringCalculator() {}

    public static Result calculate(PlayerStatistics statistic, List<ScoringRule> scoringRules) {
        interface EventCountLookup {
            int countFor(PlayerStatistics stats);
        }

        Map<String, EventCountLookup> countLookups = Map.of(
                "TRY", PlayerStatistics::getTries,
                "CONVERSION", PlayerStatistics::getConversions,
                "PENALTY", PlayerStatistics::getPenalties,
                "ASSIST", PlayerStatistics::getAssists,
                "METERS_GAINED", PlayerStatistics::getMetersGained,
                "TACKLE", PlayerStatistics::getTackles,
                "RED_CARD", PlayerStatistics::getRedCards,
                "YELLOW_CARD", PlayerStatistics::getYellowCards,
                "MISSED_TACKLE", PlayerStatistics::getMissedTackles
        );

        int total = 0;
        List<FantasyPointBreakdown> breakdowns = new ArrayList<>();

        for (ScoringRule rule : scoringRules) {
            EventCountLookup lookup = countLookups.get(rule.getEventType());
            if (lookup == null) continue;

            int eventCount = lookup.countFor(statistic);
            if (eventCount > 0) {
                int contribution = eventCount * rule.getPointsAwarded();
                if (Boolean.TRUE.equals(rule.getIsDeduction())) contribution = -contribution;
                total += contribution;

                breakdowns.add(FantasyPointBreakdown.builder()
                        .ruleId(rule.getRuleId())
                        .eventCount(eventCount)
                        .pointsEarned(contribution)
                        .build());
            }
        }

        return new Result(total, breakdowns);
    }

    public record Result(int totalPoints, List<FantasyPointBreakdown> breakdowns) {}
}
