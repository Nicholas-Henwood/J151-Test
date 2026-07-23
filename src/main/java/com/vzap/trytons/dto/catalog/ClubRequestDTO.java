package com.vzap.trytons.dto.catalog;

import com.fasterxml.jackson.annotation.JsonProperty;
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
public class ClubRequestDTO {
    private String clubName;
    private String location;
    private String homeVenue;

    @JsonProperty("isActive")
    private boolean isActive;
}
