package za.ac.vzap.trytons.frontend.client.publicpreview;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.Setter;

/**
 * Marketing-safe public league preview for the pre-auth landing page.
 * Mirrors the backend {@code PublicLeaguePreviewDTO}: no ids, no invite code.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@Getter
@Setter
public class PublicLeagueResponse {
    private String leagueName;
    private String description;
    private int maxMembers;
    private int memberCount;
}
