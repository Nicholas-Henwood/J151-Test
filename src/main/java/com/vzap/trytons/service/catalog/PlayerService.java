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
    PlayerResponseDTO updatePlayer(UUID playerId, PlayerRequestDTO request);
}