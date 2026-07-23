package com.vzap.trytons.dto.fixture;

import com.vzap.trytons.enums.FantasyRoundStatus;
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
public class DeadlineStatusResponseDTO {
    private UUID roundId;

    private FantasyRoundStatus roundStatus;

    private LocalDateTime openDate;
    private LocalDateTime lockDeadline;
    private LocalDateTime endDate;

    private boolean locked;
    private boolean openForTransfers;

    private String message;
}
