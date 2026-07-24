package com.vzap.trytons.service.catalog;

import com.vzap.trytons.dto.catalog.PlayerRequestDTO;
import com.vzap.trytons.dto.catalog.PlayerResponseDTO;

import java.util.List;
import java.util.UUID;

public interface PlayerService {
    PlayerResponseDTO createPlayer(PlayerRequestDTO request);
    PlayerResponseDTO getPlayer(UUID playerId);
    List<PlayerResponseDTO> getAllPlayers();
    List<PlayerResponseDTO> searchPlayers(String playerName, UUID clubId, UUID positionId);

    /**
     * As {@link #searchPlayers(String, UUID, UUID)}, but when availableOnly is true the
     * result is restricted to rostered players who are currently available for selection
     * (isActive AND ACTIVE in the availability table).
     */
    List<PlayerResponseDTO> searchPlayers(String playerName, UUID clubId, UUID positionId, Boolean availableOnly);
    PlayerResponseDTO updatePlayer(UUID playerId, PlayerRequestDTO request);
}