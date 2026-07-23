package com.vzap.trytons.dto.simulation;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CompetitionProcessingSummaryDTO {
    private LocalDateTime processedAt;

    private int roundsLocked;
    private int fixturesSimulated;
    private int fixturesProcessed;
    private int leaderboardsRefreshed;
    private int skipped;
    private int errors;

    private List<String> errorMessages;
    private List<String> skippedMessages;
}
