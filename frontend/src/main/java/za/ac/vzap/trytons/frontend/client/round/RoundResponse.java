package za.ac.vzap.trytons.frontend.client.round;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class RoundResponse {
    private String roundId;
    private String season;
    private int roundNumber;
    private LocalDateTime openDate;
    private LocalDateTime lockDeadline;
    private LocalDateTime endDate;
    private String status;
}
