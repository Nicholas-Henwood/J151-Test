package com.vzap.trytons.dto.league;

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
public class JoinLeagueRequestDTO {
    private String leagueCode; // Will be null for public leagues

    private UUID leagueId;
    private UUID teamId;
}
