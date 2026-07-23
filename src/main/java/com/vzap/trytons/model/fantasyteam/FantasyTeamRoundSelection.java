package com.vzap.trytons.model.fantasyteam;

import com.vzap.trytons.enums.SquadRole;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
public class FantasyTeamRoundSelection {
    private UUID selectionId;
    private UUID roundId;
    private UUID teamId;
    private UUID playerId;

    private LocalDateTime selectedDate;

    private SquadRole squadRole;

    private Boolean isCaptain;
    private Boolean isViceCaptain;

    private LocalDateTime lockedAt;
}