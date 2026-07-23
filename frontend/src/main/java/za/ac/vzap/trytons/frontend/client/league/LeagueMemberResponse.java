package za.ac.vzap.trytons.frontend.client.league;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class LeagueMemberResponse {
    private String membershipId;
    private String userId;
    private String userDisplayName;
    private String teamId;
    private String teamDisplayName;
    private LocalDateTime joinDate;
    @JsonProperty("isActive")
    private boolean isActive;
}
