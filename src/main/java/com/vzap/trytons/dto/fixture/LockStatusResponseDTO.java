package com.vzap.trytons.dto.fixture;

import com.vzap.trytons.enums.FantasyRoundStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LockStatusResponseDTO {
    private UUID roundId;

    private FantasyRoundStatus roundStatus;

    private List<UUID> lockedPlayerIds;
    private List<UUID> lockedTeamIds;

    private boolean locked;
    private boolean snapshotsCreated;

    private String message;
}
