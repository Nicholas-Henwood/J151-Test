package com.vzap.trytons.dto.fantasyteam;

import lombok.*;

import java.util.List;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ViewOpponentTeamDTO {
    private UUID teamId;

    private String teamName;

    private int totalPoints;
    private int weeklyPoints;

    private List<FantasyTeamPlayerSelectionResponseDTO> players;
}
