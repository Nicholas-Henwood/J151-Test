package za.ac.vzap.trytons.frontend.client.simulation;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class SimulationSettingRequest {
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
}
