package za.ac.vzap.trytons.frontend.client.fixture;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import lombok.Setter;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class FixtureRequest {
    private UUID leagueId;
    private UUID roundId;
    private UUID teamAId;
    private UUID teamBId;
    private LocalDate fixtureDate;
    private LocalTime fixtureTime;
    private String fixtureStatus;

}
