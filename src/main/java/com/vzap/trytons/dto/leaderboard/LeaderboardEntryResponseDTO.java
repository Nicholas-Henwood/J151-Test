package com.vzap.trytons.dto.leaderboard;

import lombok.*;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LeaderboardEntryResponseDTO {
    private UUID teamId;

    private String teamName;
    private String owner;

    private int rank;

    private Integer rankMovement;
    private Integer previousRanking;

    private int matchesPlayed;
    private int matchesWon;
    private int matchesDrawn;
    private int matchesLost;
    private int pointsFor;
    private int pointsAgainst;
    private int pointsDifference;
    private int leaguePoints;
    private int totalFantasyPoints;
}
