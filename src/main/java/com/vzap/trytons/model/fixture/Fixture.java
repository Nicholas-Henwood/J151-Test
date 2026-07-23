package com.vzap.trytons.model.fixture;

import com.vzap.trytons.enums.FixtureStatus;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.UUID;

@NoArgsConstructor
@Getter
@Setter
@AllArgsConstructor
@Builder
public class Fixture {
    private UUID fixtureId;
    private UUID leagueId;
    private UUID roundId;
    private UUID teamAId;
    private UUID teamBId;

    private LocalDate fixtureDate;
    private LocalTime fixtureTime;

    private FixtureStatus status;

    private LocalDateTime simulationDate;
    private LocalDateTime createdAt;
}