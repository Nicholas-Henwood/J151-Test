package com.vzap.trytons.dto.fantasyteam;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FantasyTeamRequestDTO {
    private String teamName;

    private List<FantasyTeamPlayerSelectionRequestDTO> selectedPlayers;
}
