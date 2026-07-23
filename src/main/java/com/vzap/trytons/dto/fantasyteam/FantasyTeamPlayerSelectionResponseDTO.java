package com.vzap.trytons.dto.fantasyteam;

import com.vzap.trytons.enums.SquadRole;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FantasyTeamPlayerSelectionResponseDTO {
    private UUID playerId;

    private String playerName;

    private UUID positionId;

    private String positionName;

    private UUID clubId;

    private String clubName;

    private BigDecimal value;

    private Boolean isActive;

    private Integer attackingAbility;
    private Integer defensiveAbility;
    private Integer kickingAbility;
    private Integer discipline;
    private Integer consistency;
    private Integer fitness;
    private Integer currentForm;
    private Integer totalFantasyPoints;

    private SquadRole squadRole;

    private Boolean isCaptain;
    private Boolean isViceCaptain;
}
