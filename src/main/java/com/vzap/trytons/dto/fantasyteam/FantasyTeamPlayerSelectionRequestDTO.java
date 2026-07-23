package com.vzap.trytons.dto.fantasyteam;

import com.vzap.trytons.enums.SquadRole;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FantasyTeamPlayerSelectionRequestDTO {
    private UUID playerId;

    private SquadRole squadRole;

    private Boolean isCaptain;
    private Boolean isViceCaptain;
}
