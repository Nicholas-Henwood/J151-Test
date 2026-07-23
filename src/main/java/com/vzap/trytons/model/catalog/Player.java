package com.vzap.trytons.model.catalog;

import lombok.*;

import java.math.BigDecimal;
import java.util.UUID;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class Player {
    private UUID playerId;
    private UUID clubId;
    private UUID positionId;

    private String playerName;
    private BigDecimal value;

    private int attackingAbility;
    private int defensiveAbility;
    private int kickingAbility;
    private int discipline;
    private int consistency;
    private int fitness;
    private int currentForm;
    private boolean isActive;
}