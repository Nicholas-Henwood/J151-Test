package com.vzap.trytons.dao.catalog;

import com.vzap.trytons.enums.AvailabilityStatus;
import com.vzap.trytons.model.catalog.Player;
import com.vzap.trytons.model.catalog.PlayerAvailability;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PlayerDAO {

    Optional<Player> getPlayerById(UUID playerId);
    List<Player> getAllPlayers();
    List<Player> searchPlayers(String playerName, UUID clubId, UUID positionId, BigDecimal minValue, BigDecimal maxValue, Integer minCurrentForm, Integer maxCurrentForm, AvailabilityStatus availabilityStatus, Boolean isActive);
    Optional<Player> createPlayer(Player player);
    Optional<Player> updatePlayer(Player player);
    boolean deactivatePlayer(UUID playerId);
    List<Player> getPlayersByClubId(UUID clubId);
    List<Player> getPlayersByPositionId(UUID positionId);
    Optional<PlayerAvailability> getCurrentAvailability(UUID playerId);
    List<PlayerAvailability> getAvailabilityHistory(UUID playerId);
}