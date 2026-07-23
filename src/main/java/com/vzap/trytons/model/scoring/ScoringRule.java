package com.vzap.trytons.model.scoring;

import lombok.*;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ScoringRule {
    private UUID ruleId;

    private String season;
    private String eventType;

    private int pointsAwarded;

    private Boolean isDeduction;

    private String description;

    private Boolean isActive;
}