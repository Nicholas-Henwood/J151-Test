package com.vzap.trytons.service.leaderboard;

import com.vzap.trytons.dto.leaderboard.LeaderboardRefreshResultDTO;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import com.vzap.trytons.dto.leaderboard.LeaderboardEntryResponseDTO;

public interface LeaderboardService {
    LeaderboardRefreshResultDTO refreshLeagueLeaderboard(UUID actorUserId, UUID leagueId);
    LeaderboardRefreshResultDTO refreshOverallLeaderboard(UUID actorUserId);
    List<LeaderboardEntryResponseDTO> getOverallLeaderboard(UUID actorUserId);
    List<LeaderboardEntryResponseDTO> getLeaderboardForLeague(UUID leagueId, UUID requestingUserId);
    Optional<LeaderboardEntryResponseDTO>  getRankingForTeam(UUID teamId, UUID leaderboardId, UUID requestingUserId);
}
