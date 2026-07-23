package za.ac.vzap.trytons.frontend.client.fantasyteam;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class FantasyTeamPlayerSelectionRequest {
    private UUID playerId;
    private String playerName;
    private UUID positionId;
    private String positionName;
    private UUID clubId;
    private String clubName;
    private BigDecimal value;
    @JsonProperty("isActive")
    private Boolean isActive;
    private Integer attackingAbility;
    private Integer defensiveAbility;
    private Integer kickingAbility;
    private Integer discipline;
    private Integer consistency;
    private Integer fitness;
    private Integer currentForm;
    private Integer totalFantasyPoints;
    private String squadRole;
    @JsonProperty("isCaptain")
    private Boolean isCaptain;
    @JsonProperty("isViceCaptain")
    private Boolean isViceCaptain;

    public FantasyTeamPlayerSelectionRequest(UUID playerId, String squadRole, boolean isCaptain, boolean isViceCaptain) {
        this.playerId = playerId;
        this.squadRole = squadRole;
        this.isCaptain = isCaptain;
        this.isViceCaptain = isViceCaptain;
    }
}
