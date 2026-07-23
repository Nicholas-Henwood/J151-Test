package com.vzap.trytons.dao.fantasyteam;


import com.vzap.trytons.enums.SquadRole;
import com.vzap.trytons.model.fantasyteam.PlayerSelectionCount;
import com.vzap.trytons.model.fantasyteam.TeamPlayerSelection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface FantasyTeamPlayerDAO {
    boolean addPlayerToSquad(UUID teamId, UUID playerId, SquadRole squadRole);
    void replaceSquad(UUID teamId, List<TeamPlayerSelection> squad);
    List<TeamPlayerSelection> getSquadByTeamId(UUID teamId);
    Optional<TeamPlayerSelection> findSquadEntry(UUID teamId, UUID playerId);
    boolean removePlayerFromSquad(UUID teamId, UUID playerId);
    boolean updateSquadRole(UUID teamId, UUID playerId, SquadRole squadRole);
    boolean setCaptain(UUID teamId, UUID playerId);
    boolean setViceCaptain(UUID teamId, UUID playerId);
    boolean clearCaptain(UUID teamId);
    boolean clearViceCaptain(UUID teamId);
    List<PlayerSelectionCount> findMostSelectedPlayers(int limit);
    List<UUID> getTeamIdsByPlayerId(UUID playerId);
}
