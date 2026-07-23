package za.ac.vzap.trytons.frontend.client.fixture;

import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.UUID;

@Getter
@Setter
public class LockStatusResponse {

    private UUID roundId;
    private String roundStatus;
    private List<UUID> lockedPlayerIds;
    private List<UUID> lockedTeamIds;
    private boolean locked;
    private boolean snapshotsCreated;
    private String message;
}