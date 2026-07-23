package com.vzap.trytons.dto.scoring;

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
public class FantasyPointBreakdownResponseDTO {

    private UUID breakdownId;
    private UUID pointsId;

    private String category;

    private int points;

    private String description;
}