package com.vzap.trytons.model.leaderboard;

import java.time.LocalDateTime;
import java.util.UUID;

import com.vzap.trytons.enums.LeaderboardScope;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@NoArgsConstructor
@SuperBuilder

public class Leaderboard {
    private UUID leaderboardId;
    private UUID leagueId;

    private String season;

    private LeaderboardScope scope;

    private LocalDateTime lastUpdated;
}
