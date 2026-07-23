package com.vzap.trytons.dto.history;

import lombok.*;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder

public class WeeklyPerformanceResponseDTO {
    private UUID roundId;
    private UUID fixtureId;

    private int pointsScored;

    private String result;
}