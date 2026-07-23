package com.vzap.trytons.dto.catalog;

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


public class PositionResponseDTO {
    private UUID positionId;

    private String positionName;
    private String positionCategory;

    private int minRequired;
    private int maxAllowed;
}
