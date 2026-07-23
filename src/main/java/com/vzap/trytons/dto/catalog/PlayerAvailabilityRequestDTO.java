package com.vzap.trytons.dto.catalog;

import com.vzap.trytons.enums.AvailabilityStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PlayerAvailabilityRequestDTO {
    private AvailabilityStatus status;

    private LocalDate effectiveDate;
    private LocalDate endDate;

    private String notes;
}
