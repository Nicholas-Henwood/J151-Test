package com.vzap.trytons.dao.scoring;

import com.vzap.trytons.model.scoring.ScoringRule;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ScoringRuleDAO {

    List<ScoringRule> findActiveRules(String season);
    Optional<ScoringRule> findById(UUID ruleId);
    Optional<ScoringRule> findBySeasonAndEventType(String season, String eventType);
    ScoringRule save(ScoringRule rule);
    ScoringRule update(ScoringRule rule);

    /**
     * Whether the given season already has match results. Once it does, the
     * database triggers lock the season's scoring ruleset (see the
     * trg_scoringRule_season_locked_* triggers), so rules can no longer be
     * created, changed or removed for it.
     */
    boolean seasonHasResults(String season);
}