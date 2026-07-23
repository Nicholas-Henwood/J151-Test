package com.vzap.trytons.model.fixture;

import com.vzap.trytons.enums.FantasyRoundStatus;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;


@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FantasyRound {
    private UUID roundId;

    private String season;

    private int roundNumber;

    private LocalDateTime openDate;
    private LocalDateTime lockDeadline;
    private LocalDateTime endDate;

    private FantasyRoundStatus status;
}
