package com.vzap.trytons.model.catalog;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@NoArgsConstructor
@Getter
@Setter
public class Position {
    private UUID positionId;

    private String positionName;
    private String positionCategory;

    private int minRequired;
    private int maxAllowed;
}