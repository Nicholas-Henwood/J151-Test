package com.vzap.trytons.dao.results;

import com.vzap.trytons.model.results.PlayerStatistics;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PlayerStatisticsDAO {
    List<PlayerStatistics> findByResultId(UUID resultId);
    List<PlayerStatistics> findByResultIdAndTeamId(UUID resultId, UUID teamId);
    Optional<PlayerStatistics> findByResultIdAndTeamIdAndPlayerId(UUID resultId, UUID teamId, UUID playerId);
    Optional<PlayerStatistics> save(PlayerStatistics playerStatistics);
    Optional<PlayerStatistics> findById(UUID statId);
}
