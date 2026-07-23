package com.vzap.trytons.dto.league;

import com.vzap.trytons.enums.LeagueType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LeagueResponseDTO {
    private UUID leagueId;
    private UUID managerUserId;

    private String leagueName;
    private String description;

    private LeagueType leagueType;

    private LocalDateTime creationDate;

    private Boolean isActive;

    private int maxMembers;

    private String leagueCode;
}
