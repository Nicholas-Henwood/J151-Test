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
public class ScoringRuleResponseDTO {
    private UUID ruleId;

    private String eventType;

    private int pointsAwarded;

    private String season;

    private boolean active;

    Boolean isDeduction;

    String description;
}