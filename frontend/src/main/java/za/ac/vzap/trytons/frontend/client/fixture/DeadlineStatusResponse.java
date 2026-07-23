package za.ac.vzap.trytons.frontend.client.fixture;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
public class DeadlineStatusResponse {
    private UUID roundId;
    private String roundStatus;
    private LocalDateTime openDate;
    private LocalDateTime lockDeadline;
    private LocalDateTime endDate;
    private boolean locked;
    private boolean openForTransfers;
    private String message;
}
