package com.vzap.trytons.dto.results;

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

public class TeamScoreUpdateResultDTO {

    private String fixtureId;
    private String teamId;

    private int teamATotal;
    private int teamBTotal;
    private int seasonTotal;

    private String outcome;

}
