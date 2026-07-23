package com.vzap.trytons.dto.fixture;

import com.vzap.trytons.enums.FixtureStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FixtureRequestDTO {
    private UUID leagueId;
    private UUID roundId;
    private UUID teamAId;
    private UUID teamBId;

    private FixtureStatus fixtureStatus;

    private LocalDate fixtureDate;
    private LocalTime fixtureTime;
}
