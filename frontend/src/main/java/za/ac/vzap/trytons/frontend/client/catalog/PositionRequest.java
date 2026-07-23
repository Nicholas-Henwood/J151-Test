package za.ac.vzap.trytons.frontend.client.catalog;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class PositionRequest {
    private String positionName;
    private String positionCategory;
    private int minRequired;
    private int maxAllowed;
}
