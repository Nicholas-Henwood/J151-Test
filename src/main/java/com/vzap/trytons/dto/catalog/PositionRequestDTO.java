package com.vzap.trytons.dto.catalog;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PositionRequestDTO {
    private String positionName;
    private String positionCategory;

    private int minRequired;
    private int maxAllowed;
}
