package com.vzap.trytons.service.history;

import com.vzap.trytons.dto.history.UserPointsHistoryResponseDTO;
import com.vzap.trytons.dto.history.WeeklyPerformanceResponseDTO;

import java.util.List;
import java.util.UUID;

public interface UserHistoryService {
    UserPointsHistoryResponseDTO getUserPointsHistory(UUID actorUserId);
    List<WeeklyPerformanceResponseDTO> getWeeklyPerformance(UUID actorUserId);
}