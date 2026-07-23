package com.vzap.trytons.dto.fantasyteam;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ViewOwnTeamDTO {
    private UUID teamId;

    private String teamName;

    private BigDecimal totalTeamValue;
    private BigDecimal remainingBudget;

    private LocalDateTime creationDate;

    private int totalPoints;

    private Boolean isValid;

    private String ownerUsername;

    private List<FantasyTeamPlayerSelectionResponseDTO> players;
}
