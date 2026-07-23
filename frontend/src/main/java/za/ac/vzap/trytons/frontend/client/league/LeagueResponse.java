package za.ac.vzap.trytons.frontend.client.league;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
public class LeagueResponse {
    private String leagueId;
    private UUID managerUserId;
    private String leagueName;
    private String description;
    private String leagueType;
    private LocalDateTime creationDate;
    @JsonProperty("isActive")
    private Boolean isActive;
    private int maxMembers;
    private String managerDisplayName;
    private String leagueCode;
}