package com.vzap.trytons.model.scoring;

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
public class FantasyPointBreakdown {
    private UUID breakdownId;
    private UUID pointsId;
    private UUID ruleId;

    private int eventCount;
    private int pointsEarned;
}