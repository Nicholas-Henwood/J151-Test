package com.vzap.trytons.model.catalog;

import com.vzap.trytons.enums.AvailabilityStatus;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.util.UUID;

@NoArgsConstructor
@Getter
@Setter
public class PlayerAvailability {
    private UUID availabilityId;

    private AvailabilityStatus status;

    private LocalDate effectiveDate;
    private LocalDate endDate;

    private String notes;

    private UUID playerId;
}