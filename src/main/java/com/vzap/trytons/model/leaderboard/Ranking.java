package com.vzap.trytons.model.leaderboard;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;
import java.util.UUID;

@NoArgsConstructor
@Getter
@Setter
@SuperBuilder

public class Ranking {
    private UUID rankingId;
    private UUID leaderboardId;
    private UUID teamId;

    private int currentRanking;

    private Integer previousRanking;

    private int matchesPlayed;
    private int matchesWon;
    private int matchesDrawn;
    private int matchesLost;
    private int pointsFor;
    private int pointsAgainst;
    private int scoreDifference;
    private int leaguePoints;
    private int totalFantasyPoints;

    private LocalDateTime updatedAt;
}