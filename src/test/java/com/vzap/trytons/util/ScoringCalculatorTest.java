package com.vzap.trytons.util;

import com.vzap.trytons.model.results.PlayerStatistics;
import com.vzap.trytons.model.scoring.ScoringRule;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * ScoringCalculator is the single source of truth for turning player statistics into fantasy points.
 * Two separate paths depend on it agreeing with itself: MatchSimulationServiceImpl uses it to produce the
 * match score stored on matchResult, and FantasyPointCalculationServiceImpl uses it to produce the
 * fantasyPoints that TeamScoreServiceImpl later sums into match_team_score. A database trigger
 * (trg_match_team_score_insert) rejects the breakdown if those two numbers disagree.
 *
 * These tests pin the behaviour both paths rely on, so a change here has to be a deliberate one.
 */
class ScoringCalculatorTest {

    private static ScoringRule rule(String eventType, int pointsAwarded, boolean isDeduction) {
        return ScoringRule.builder()
                .ruleId(UUID.randomUUID())
                .season("2026")
                .eventType(eventType)
                .pointsAwarded(pointsAwarded)
                .isDeduction(isDeduction)
                .isActive(true)
                .build();
    }

    @Test
    void multipliesEventCountByPointsAwarded() {
        PlayerStatistics statistics = PlayerStatistics.builder().tries(3).build();

        assertEquals(15, ScoringCalculator.calculate(statistics, List.of(rule("TRY", 5, false))).totalPoints());
    }

    @Test
    void sumsAcrossEveryMatchingRule() {
        PlayerStatistics statistics = PlayerStatistics.builder()
                .tries(1)
                .assists(1)
                .tackles(7)
                .build();

        List<ScoringRule> rules = List.of(
                rule("TRY", 5, false),
                rule("ASSIST", 3, false),
                rule("TACKLE", 1, false));

        // Mirrors the seeded reference case: 1*5 + 1*3 + 7*1.
        assertEquals(15, ScoringCalculator.calculate(statistics, rules).totalPoints());
    }

    @Test
    void subtractsWhenTheRuleIsADeduction() {
        PlayerStatistics statistics = PlayerStatistics.builder()
                .tries(2)
                .yellowCards(1)
                .build();

        List<ScoringRule> rules = List.of(
                rule("TRY", 5, false),
                rule("YELLOW_CARD", 3, true));

        assertEquals(7, ScoringCalculator.calculate(statistics, rules).totalPoints());
    }

    @Test
    void ignoresEventTypesWithNoMatchingStatistic() {
        PlayerStatistics statistics = PlayerStatistics.builder().tries(1).build();

        List<ScoringRule> rules = List.of(
                rule("TRY", 5, false),
                rule("NOT_A_REAL_EVENT", 100, false));

        assertEquals(5, ScoringCalculator.calculate(statistics, rules).totalPoints());
    }

    @Test
    void recordsNoBreakdownForAnEventThatDidNotOccur() {
        PlayerStatistics statistics = PlayerStatistics.builder().tries(1).assists(0).build();

        List<ScoringRule> rules = List.of(
                rule("TRY", 5, false),
                rule("ASSIST", 3, false));

        ScoringCalculator.Result result = ScoringCalculator.calculate(statistics, rules);

        assertEquals(1, result.breakdowns().size());
        assertEquals(5, result.breakdowns().get(0).getPointsEarned());
    }

    @Test
    void breakdownsAccountForTheWholeTotal() {
        PlayerStatistics statistics = PlayerStatistics.builder()
                .tries(2)
                .tackles(4)
                .missedTackles(3)
                .build();

        List<ScoringRule> rules = List.of(
                rule("TRY", 5, false),
                rule("TACKLE", 1, false),
                rule("MISSED_TACKLE", 1, true));

        ScoringCalculator.Result result = ScoringCalculator.calculate(statistics, rules);

        int summedBreakdowns = result.breakdowns().stream()
                .mapToInt(breakdown -> breakdown.getPointsEarned())
                .sum();

        // The stored breakdown must explain the stored total, or the audit trail is misleading.
        assertEquals(result.totalPoints(), summedBreakdowns);
        assertEquals(11, result.totalPoints());
    }

    @Test
    void scoresNothingWhenNoRulesAreActive() {
        PlayerStatistics statistics = PlayerStatistics.builder().tries(3).tackles(9).build();

        ScoringCalculator.Result result = ScoringCalculator.calculate(statistics, List.of());

        assertEquals(0, result.totalPoints());
        assertTrue(result.breakdowns().isEmpty());
    }
}
