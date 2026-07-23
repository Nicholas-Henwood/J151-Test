package com.vzap.trytons.service.catalog;

import com.vzap.trytons.dto.catalog.PlayerAvailabilityRequestDTO;
import com.vzap.trytons.dto.catalog.PlayerAvailabilityResponseDTO;

import java.util.Optional;
import java.util.UUID;

public interface PlayerAvailabilityService {
    PlayerAvailabilityResponseDTO setAvailability(UUID actorUserId, UUID playerId, PlayerAvailabilityRequestDTO request);
    Optional<PlayerAvailabilityResponseDTO> getAvailability(UUID playerId);
}
