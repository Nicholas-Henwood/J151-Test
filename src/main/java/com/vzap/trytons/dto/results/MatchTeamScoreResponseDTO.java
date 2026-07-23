package com.vzap.trytons.dto.results;

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
public class MatchTeamScoreResponseDTO {
    private UUID teamScoreId;
    private UUID resultId;
    private UUID teamId;

    private MatchTeamSide teamSide;

    private int playerPoints;
    private int captainBonus;
    private int transferPenalty;
    private int totalScore;

    private LocalDateTime calculatedAt;
}