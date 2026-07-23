package za.ac.vzap.trytons.frontend.client.transfer;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
public class TransferResponse {
    private UUID transferId;
    private UUID teamId;
    private UUID roundId;

    private UUID removedPlayerId;
    private String removedPlayerName;
    private BigDecimal removedPlayerValue;

    private UUID addedPlayerId;
    private String addedPlayerName;
    private BigDecimal addedPlayerValue;

    private BigDecimal valueDifference;
    private int penaltyPoints;

    private String status;
    private LocalDateTime transferDate;
    private LocalDateTime confirmationDate;
}
