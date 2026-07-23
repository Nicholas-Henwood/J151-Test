package za.ac.vzap.trytons.frontend.client.catalog;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ClubRequest {
    private String clubName;
    private String location;
    private String homeVenue;
    @JsonProperty("isActive")
    private boolean isActive;
}
