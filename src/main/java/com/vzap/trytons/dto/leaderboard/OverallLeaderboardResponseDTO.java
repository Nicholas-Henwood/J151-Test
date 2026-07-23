package com.vzap.trytons.dto.leaderboard;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder

public class OverallLeaderboardResponseDTO {
    private String season;

    private List<LeaderboardEntryResponseDTO> standings;
}