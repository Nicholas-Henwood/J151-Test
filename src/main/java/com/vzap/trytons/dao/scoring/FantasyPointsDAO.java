package com.vzap.trytons.dao.scoring;

import com.vzap.trytons.model.scoring.FantasyPoints;
import com.vzap.trytons.model.scoring.PlayerPointSummary;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface FantasyPointsDAO {
    FantasyPoints save(FantasyPoints fantasyPoints);
    Optional<FantasyPoints> findById(UUID pointsId);
    List<FantasyPoints> findByStatId(UUID statId);
    Optional<FantasyPoints> findFinalByStatId(UUID statId);
    int markExistingPointsForStatAsNotFinal(UUID statId);
    int getNextCalculationVersion(UUID statId);
    int getTotalFinalPointsForPlayer(UUID playerId);
    List<PlayerPointSummary> findTopPlayerByFinalPoints(int limit);
}