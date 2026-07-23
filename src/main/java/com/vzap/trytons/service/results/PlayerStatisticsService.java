package com.vzap.trytons.service.results;

import com.vzap.trytons.dto.results.PlayerStatisticsRequestDTO;
import com.vzap.trytons.dto.results.PlayerStatisticsResponseDTO;

import java.util.List;
import java.util.UUID;

public interface PlayerStatisticsService {
    List<PlayerStatisticsResponseDTO> listResultStatistics(UUID resultId);
    List<PlayerStatisticsResponseDTO> listResultStatisticsForTeam(UUID resultId, UUID teamId);
    PlayerStatisticsResponseDTO captureStatistic(UUID actorUserId, PlayerStatisticsRequestDTO request);
}