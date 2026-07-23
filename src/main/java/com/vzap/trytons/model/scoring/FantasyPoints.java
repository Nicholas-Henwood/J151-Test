package com.vzap.trytons.model.scoring;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FantasyPoints {
    private UUID pointsId;
    private UUID statId;

    private int totalPoints;
    private int calculationVersion;

    private boolean isFinal;

    private LocalDateTime calculatedAt;
}