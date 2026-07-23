package za.ac.vzap.trytons.frontend.client.catalog;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.time.LocalDate;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class PlayerAvailabilityRequest {
    private String status;
    private LocalDate effectiveDate;
    private LocalDate endDate;
    private String notes;
}
