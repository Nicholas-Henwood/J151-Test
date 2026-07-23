package com.vzap.trytons.dto.catalog;

import com.fasterxml.jackson.annotation.JsonProperty;
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

public class ClubResponseDTO {
    private UUID clubId;

    private String clubName;
    private String location;
    private String homeVenue;

    @JsonProperty("isActive")
    private boolean isActive;
}
