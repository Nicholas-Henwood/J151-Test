package za.ac.vzap.trytons.frontend.client.simulation;

import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class ResimulationRequest {
    private UUID fixtureId;
    private String resimulationReason;
}
