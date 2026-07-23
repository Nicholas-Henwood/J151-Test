package com.vzap.trytons.dto.results;

import com.fasterxml.jackson.annotation.JsonProperty;
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
public class MatchResultResponseDTO {
    private UUID resultId;
    private UUID fixtureId;
    private UUID teamAId;
    private UUID teamBId;

    private int simulationRunNumber;
    private int teamAScore;
    private int teamBScore;

    private MatchTeamSide winnerSide;

    @JsonProperty("isDraw")
    private boolean isDraw;
    private boolean approved;

    @JsonProperty("isCurrent")
    private boolean isCurrent;

    private LocalDateTime resultDate;
    private UUID approvedByAdminUserId;
}