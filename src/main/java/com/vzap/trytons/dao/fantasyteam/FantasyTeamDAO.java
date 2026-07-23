package com.vzap.trytons.dao.fantasyteam;

import com.vzap.trytons.model.fantasyteam.FantasyTeam;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

public interface FantasyTeamDAO {
    Optional<FantasyTeam> createTeam(FantasyTeam team);
    Optional<FantasyTeam> getTeamById(UUID teamId);
    FantasyTeam findTeamById(UUID teamId);
    Optional<FantasyTeam> getTeamByOwner(UUID owner_user_id);
    boolean updateBudget(UUID teamId, BigDecimal remainingBudget);
    boolean updateTeamName(UUID teamId, String teamName);
}
