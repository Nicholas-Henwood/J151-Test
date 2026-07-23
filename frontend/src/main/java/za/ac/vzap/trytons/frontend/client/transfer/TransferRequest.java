package za.ac.vzap.trytons.frontend.client.transfer;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class TransferRequest {
    private UUID teamId;
    private UUID roundId;
    private UUID removedPlayerId;
    private UUID addedPlayerId;
    private boolean penaltyConfirmed;
}