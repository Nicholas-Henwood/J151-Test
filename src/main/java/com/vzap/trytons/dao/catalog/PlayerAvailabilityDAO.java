package com.vzap.trytons.dao.catalog;

import com.vzap.trytons.model.catalog.PlayerAvailability;

import java.util.Optional;
import java.util.UUID;

public interface PlayerAvailabilityDAO {

    Optional<PlayerAvailability> getCurrentByPlayer(UUID playerId);
    PlayerAvailability upsert(PlayerAvailability availability);
}
