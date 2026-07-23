package za.ac.vzap.trytons.frontend.client.simulation;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
public class ResimulationResponse {
    private UUID previousResultId;
    private UUID newResultId;
    private UUID fixtureId;
    private int simulationRunNumber;
    private boolean current;
    private boolean approved;
    private String resimulationReason;
    private LocalDateTime resimulatedAt;
}
