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

    /**
     * Read-only master leaderboard preview for the pre-auth landing page.
     * No user context required. limit &lt;= 0 returns all entries.
     */
    List<LeaderboardEntryResponseDTO> getPublicOverallLeaderboard(int limit);
    List<LeaderboardEntryResponseDTO> getLeaderboardForLeague(UUID leagueId, UUID requestingUserId);
    Optional<LeaderboardEntryResponseDTO>  getRankingForTeam(UUID teamId, UUID leaderboardId, UUID requestingUserId);
}
