package com.vzap.trytons.dto.simulation;

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
public class ResimulationResponseDTO {
    private UUID previousResultId;
    private UUID newResultId;
    private UUID fixtureId;
    private int simulationRunNumber;
    private boolean current;
    private boolean approved;
    private String resimulationReason;
    private LocalDateTime resimulatedAt;
}