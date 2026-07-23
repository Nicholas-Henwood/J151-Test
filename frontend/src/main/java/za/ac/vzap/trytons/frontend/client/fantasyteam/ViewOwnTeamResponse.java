package za.ac.vzap.trytons.frontend.client.fantasyteam;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Setter
@Getter
public class ViewOwnTeamResponse {
    private UUID teamId;
    private String teamName;
    private BigDecimal totalTeamValue;
    private BigDecimal remainingBudget;
    private LocalDateTime creationDate;
    private int totalPoints;
    @JsonProperty("isValid")
    private Boolean isValid;
    private String ownerUsername;
    private List<FantasyTeamPlayerSelectionResponse> players;
}
