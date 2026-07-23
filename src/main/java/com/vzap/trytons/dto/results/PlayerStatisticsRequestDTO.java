package com.vzap.trytons.dto.results;

import lombok.*;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PlayerStatisticsRequestDTO {
    private UUID resultId;
    private UUID teamId;
    private UUID playerId;

    private int tries;
    private int assists;
    private int tackles;
    private int missedTackles;
    private int conversions;
    private int penalties;
    private int metersGained;
    private int yellowCards;
    private int redCards;
}
