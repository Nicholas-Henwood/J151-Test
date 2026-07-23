package com.vzap.trytons.model.transfer;

import com.vzap.trytons.enums.TransferStatus;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@NoArgsConstructor
@Getter
@Setter
public class Transfer {
    private UUID transferId;

    private LocalDateTime transferDate;
    private LocalDateTime confirmedAt;

    private int penaltyPoints;

    private BigDecimal removedPlayerValue;
    private BigDecimal addedPlayerValue;
    private BigDecimal valueDifference;

    private TransferStatus status;

    private UUID roundId;
    private UUID teamId;
    private UUID removedPlayerId;

    private String removedPlayerName;
    private String addedPlayerName;

    private UUID addedPlayerId;
    private UUID createdByUserId;
}