package za.ac.vzap.trytons.frontend.client.catalog;

import lombok.Getter;
import lombok.Setter;

import java.util.UUID;
@Getter
@Setter
public class PositionResponse {
    private UUID positionId;
    private String positionName;
    private String positionCategory;
    private int minRequired;
    private int maxAllowed;
}
