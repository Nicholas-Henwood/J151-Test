package com.vzap.trytons.dto.league;

import com.vzap.trytons.enums.LeagueType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LeagueRequestDTO {
    private String leagueName;
    private String description;

    private LeagueType leagueType;

    private int maxMembers;
}
