package com.vzap.trytons.model.results;

import com.vzap.trytons.enums.MatchTeamSide;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MatchResult {
    private UUID resultId;
    private UUID fixtureId;
    private UUID settingsId;

    private int simulationRunNumber;
    private int teamAScore;
    private int teamBScore;

    private MatchTeamSide winnerSide;

    private boolean isDraw;
    private boolean approved;
    private boolean isCurrent;

    private LocalDateTime resultDate;

    private UUID approvedByAdminUserId;
}