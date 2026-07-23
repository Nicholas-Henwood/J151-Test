package za.ac.vzap.trytons.frontend.client.transfer;

import lombok.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder

public class TransferRecommendationResponse {
    private List<RecommendedPlayer> recommendations;
    private UUID teamId;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class RecommendedPlayer {
        private UUID playerId;
        private String playerName;
        private String positionName;
        private String clubName;
        private BigDecimal value;
        private int currentForm;
        private String availabilityStatus;
        private UUID replacesPlayerId;
        private String reason;
    }
}
