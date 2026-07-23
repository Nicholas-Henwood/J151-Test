package za.ac.vzap.trytons.frontend.client.transfer;

import lombok.Getter;
import lombok.Setter;

import java.util.UUID;
@Getter
@Setter
public class TransferRecommendationRequest {
    private UUID teamId;
    private UUID currentPlayerId;
}
