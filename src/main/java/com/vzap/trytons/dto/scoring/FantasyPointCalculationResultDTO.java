package com.vzap.trytons.dto.scoring;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder

public class FantasyPointCalculationResultDTO {
    private String fixtureId;

    private int pointsRowsWritten;
    private int calculationVersion;
}