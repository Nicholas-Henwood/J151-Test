package com.vzap.trytons.dto.simulation;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SimulationSettingRequestDTO {
    private String season;

    private BigDecimal playerAbilityWeight;
    private BigDecimal playerFormWeight;
    private BigDecimal teamBalanceWeight;
    private BigDecimal randomVariationWeight;

    private Boolean requireAdminApproval;
    private Boolean allowResimulation;

    private int maxResimulations;

    private Boolean isActive;
}