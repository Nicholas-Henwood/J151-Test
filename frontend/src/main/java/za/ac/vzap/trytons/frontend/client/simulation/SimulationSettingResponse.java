package za.ac.vzap.trytons.frontend.client.simulation;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
public class SimulationSettingResponse {
    private UUID settingsId;
    private String season;
    private BigDecimal playerAbilityWeight;
    private BigDecimal playerFormWeight;
    private BigDecimal teamBalanceWeight;
    private BigDecimal randomVariationWeight;
    private Boolean requireAdminApproval;
    private Boolean allowResimulation;
    private int maxResimulations;
    @JsonProperty("isActive")
    private Boolean isActive;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
