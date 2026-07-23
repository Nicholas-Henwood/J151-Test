package com.vzap.trytons.model.catalog;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@NoArgsConstructor
@Getter
@Setter
public class Club {
    private UUID clubId;

    private String clubName;
    private String location;
    private String homeVenue;

    private boolean isActive;
}