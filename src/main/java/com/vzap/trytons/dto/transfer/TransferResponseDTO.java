package com.vzap.trytons.dto.transfer;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TransferResponseDTO {
    private UUID transferId;
    private UUID teamId;
    private UUID roundId;
    private UUID removedPlayerId;
    private UUID addedPlayerId;

    private String removedPlayerName;
    private String addedPlayerName;

    private BigDecimal removedPlayerValue;
    private BigDecimal addedPlayerValue;
    private BigDecimal valueDifference;

    private int penaltyPoints;

    private String status;

    private LocalDateTime transferDate;
    private LocalDateTime confirmationDate;
}
