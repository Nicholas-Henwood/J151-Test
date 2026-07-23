package com.vzap.trytons.dto.leaderboard;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LeaderboardRefreshResultDTO {
    private boolean success;

    private String message;

    private int teamsProcessed;
    private int rankingsUpdated;
}
