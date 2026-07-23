package com.vzap.trytons.dto.league;

import lombok.*;

import java.util.UUID;


@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class JoinLeagueResponseDTO {
    private UUID leagueId;

    private String leagueName;
    private String message;

    private UUID membershipId;
}
