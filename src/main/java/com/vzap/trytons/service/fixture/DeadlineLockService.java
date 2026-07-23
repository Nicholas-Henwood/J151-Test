package com.vzap.trytons.service.fixture;

import com.vzap.trytons.dto.fixture.DeadlineStatusResponseDTO;
import com.vzap.trytons.dto.fixture.LockStatusResponseDTO;
import com.vzap.trytons.model.catalog.Player;

import java.util.List;
import java.util.UUID;

public interface DeadlineLockService {
    LockStatusResponseDTO getLockStatus(UUID roundId);
    DeadlineStatusResponseDTO getDeadlineStatus(UUID roundId);
    LockStatusResponseDTO lockRound(UUID actorAdminUserId, UUID roundId, String reason);
    List<UUID> getLockedTeamIds(UUID roundId);
    List<UUID> getLockedPlayerIds(UUID roundId, UUID teamId);
    List<Player> getAvailableTransferPlayers(UUID roundId, UUID teamId);
    void assertTransferAllowed(UUID roundId, UUID teamId);
}