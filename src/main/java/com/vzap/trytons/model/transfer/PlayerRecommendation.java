package com.vzap.trytons.model.transfer;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@NoArgsConstructor
@Getter
@Setter
public class PlayerRecommendation {
    private UUID recommendationId;

    private String reason;

    private BigDecimal score;

    private LocalDateTime createdAt;

    private Boolean isDismissed;

    private UUID teamId;
    private UUID currentPlayerId;
    private UUID recommendedPlayerId;
}