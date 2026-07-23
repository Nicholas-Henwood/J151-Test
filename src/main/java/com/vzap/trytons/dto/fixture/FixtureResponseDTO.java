package com.vzap.trytons.dto.fixture;

import com.vzap.trytons.enums.FixtureStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.UUID;
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder

public class FixtureResponseDTO {
    private UUID fixtureId;
    private UUID leagueId;
    private UUID roundId;
    private UUID teamAId;

    private String teamAName;

    private UUID teamBId;

    private String teamBName;

    private LocalDate fixtureDate;
    private LocalTime fixtureTime;

    private FixtureStatus fixtureStatus;

    private LocalDateTime simulationDate;
    private LocalDateTime createdAt;
}
