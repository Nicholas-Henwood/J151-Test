package za.ac.vzap.trytons.frontend.client.catalog;

import lombok.Getter;
import lombok.Setter;
import java.time.LocalDate;
import java.util.UUID;

@Getter
@Setter
public class PlayerAvailabilityResponse {
    private UUID availabilityId;
    private UUID playerId;
    private String status;
    private LocalDate effectiveDate;
    private LocalDate endDate;
    private String notes;
}
