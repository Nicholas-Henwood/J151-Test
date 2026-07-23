package com.vzap.trytons.dao.results;

import com.vzap.trytons.model.results.PlayerStatisticsCorrection;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PlayerStatisticsCorrectionDAO {
    Optional<PlayerStatisticsCorrection> save(PlayerStatisticsCorrection playerStatisticsCorrection);
    Optional<PlayerStatisticsCorrection> findById(UUID correctionId);
    Optional<List<PlayerStatisticsCorrection>> findByAdminUserId(UUID correctionByAdminUserId);
    Optional<PlayerStatisticsCorrection> findStatId(UUID correctionId);
}
