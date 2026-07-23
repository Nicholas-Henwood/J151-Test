package com.vzap.trytons.dto.results;

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
public class MatchResultRequestDTO {
    private UUID fixtureId;

    private int teamAScore;
    private int teamBScore;
}