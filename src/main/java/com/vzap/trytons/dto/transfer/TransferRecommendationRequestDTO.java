package com.vzap.trytons.dto.transfer;

import jakarta.validation.constraints.NotNull;
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
public class TransferRecommendationRequestDTO {
    @NotNull(message = "Team ID is required")
    private UUID teamId;
    private UUID currentPlayerId;
}
