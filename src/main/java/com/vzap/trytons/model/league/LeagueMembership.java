package com.vzap.trytons.model.league;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@NoArgsConstructor
@Getter
@Setter
public class LeagueMembership {
    private UUID membershipId;

    private Boolean isActive;

    private LocalDateTime joinDate;

    private UUID leagueId;
    private UUID registeredUserId;
    private UUID teamId;
}