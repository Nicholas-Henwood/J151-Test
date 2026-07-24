package za.ac.vzap.trytons.frontend.client.catalog;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * Outcome of a live-feed player import, as returned by
 * {@code POST /api/player/import}. Mirrors the backend PlayerImportSummaryDTO.
 * Unknown fields are ignored so a backend addition never breaks deserialisation.
 */
@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class PlayerImportSummaryResponse {
    private int fetched;
    private int inserted;
    private int updated;
    private int reactivated;
    private int deactivated;
    private int skipped;
    private List<String> unmappedClubIds;
    private List<String> unmappedPositionIds;
    private List<String> missingCatalogNames;
    private long durationMs;
    private String refreshedAt;
}
