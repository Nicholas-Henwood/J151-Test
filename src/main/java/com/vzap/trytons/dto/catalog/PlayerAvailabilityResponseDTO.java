package com.vzap.trytons.dto.catalog;

import com.vzap.trytons.enums.AvailabilityStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.util.UUID;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PlayerAvailabilityResponseDTO {
    private UUID availabilityId;
    private UUID playerId;

    private AvailabilityStatus status;

    private LocalDate effectiveDate;
    private LocalDate endDate;

    private String notes;
}
