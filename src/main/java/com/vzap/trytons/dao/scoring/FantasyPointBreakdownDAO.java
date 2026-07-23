package com.vzap.trytons.dao.scoring;

import com.vzap.trytons.model.scoring.FantasyPointBreakdown;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface FantasyPointBreakdownDAO {
    FantasyPointBreakdown save(FantasyPointBreakdown fantasyPointBreakdown);
    Optional<FantasyPointBreakdown> findById(UUID breakdownId);
    List<FantasyPointBreakdown> findByPointsId(UUID pointsId);
}