package com.vzap.trytons.dto.transfer;

import lombok.*;

import java.util.List;
import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class TransferRecommendationResponseDTO {
    private UUID teamId;
    private List<RecommendedPlayerDTO> recommendations;
}
