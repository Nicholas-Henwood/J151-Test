package com.vzap.trytons.dto.transfer;

import lombok.*;

import java.math.BigDecimal;
import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class RecommendedPlayerDTO {
    private UUID playerId;

    private String playerName;
    private String positionName;
    private String clubName;

    private BigDecimal value;

    private int currentForm;

    private String availabilityStatus;

    private UUID replacesPlayerId;

    private String reason;
}
