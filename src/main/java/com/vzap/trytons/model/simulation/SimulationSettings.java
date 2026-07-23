package com.vzap.trytons.model.simulation;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class SimulationSettings {
    private UUID settingsId;

    private String season;

    private BigDecimal playerAbilityWeight;
    private BigDecimal playerFormWeight;
    private BigDecimal teamBalanceWeight;
    private BigDecimal randomVariationWeight;

    private Boolean requireAdminApproval;
    private Boolean allowResimulation;

    private int maxResimulations;

    private Boolean isActive;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
