package com.vzap.trytons.dto.simulation;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class MatchProcessingResultDTO {
    private UUID fixtureId;

    private int pointsCalculated;
    private int teamsUpdated;

    private boolean leaderboardsRefreshed;

    private String status;
}
