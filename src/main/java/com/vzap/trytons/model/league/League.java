package com.vzap.trytons.model.league;

import com.vzap.trytons.enums.LeagueType;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@NoArgsConstructor
@Getter
@Setter
public class League {
    private UUID leagueId;

    private String leagueName;
    private String description;

    private LeagueType leagueType;

    private String leagueCode;

    private LocalDateTime creationDate;

    private Boolean isActive;

    private int maxMembers;

    private UUID managerUserId;
}