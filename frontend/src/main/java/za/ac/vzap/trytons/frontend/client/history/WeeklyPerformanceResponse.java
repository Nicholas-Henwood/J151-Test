package za.ac.vzap.trytons.frontend.client.history;

import lombok.*;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor

public class WeeklyPerformanceResponse {

    private UUID roundId;
    private UUID fixtureId;
    private int pointsScored;
    private String result;

}
