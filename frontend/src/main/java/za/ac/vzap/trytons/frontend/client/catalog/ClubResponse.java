package za.ac.vzap.trytons.frontend.client.catalog;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;
@Getter
@Setter
public class ClubResponse {
    private UUID clubId;
    private String clubName;
    private String location;
    private String homeVenue;
    @JsonProperty("isActive")
    private boolean isActive;
}
