package com.vzap.trytons.dto.fantasyteam;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FantasyTeamResponseDTO {
    private UUID teamId;

    private String teamName;

    private UUID managerId;

    private String managerUsername;

    private BigDecimal totalTeamValue;
    private BigDecimal remainingBudget;

    private Integer weeklyPoints;
    private Integer totalPoints;

    private Boolean valid;

    private List<FantasyTeamPlayerSelectionResponseDTO> selectedPlayers;
}
