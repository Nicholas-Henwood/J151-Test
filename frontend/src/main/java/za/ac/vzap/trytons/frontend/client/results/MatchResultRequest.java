package za.ac.vzap.trytons.frontend.client.results;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class MatchResultRequest {
    private UUID fixtureId;
    private int teamAScore;
    private int teamBScore;
}
