package com.vzap.trytons.service.fantasyteam;

import com.vzap.trytons.dto.fantasyteam.FantasyTeamRequestDTO;
import com.vzap.trytons.dto.fantasyteam.FantasyTeamResponseDTO;
import com.vzap.trytons.dto.fantasyteam.ViewOpponentTeamDTO;
import com.vzap.trytons.dto.fantasyteam.ViewOwnTeamDTO;

import java.util.UUID;

public interface FantasyTeamService {
    FantasyTeamResponseDTO createTeam(UUID registeredUserId, FantasyTeamRequestDTO fantasyTeamDTO);
    FantasyTeamResponseDTO getOwnTeam(UUID registeredUserId);
    ViewOpponentTeamDTO viewOpponentTeam(UUID teamId);
    ViewOwnTeamDTO viewOwnTeam(UUID registeredUserId, UUID teamId);
    FantasyTeamResponseDTO updateTeam(UUID registeredId, UUID teamId, FantasyTeamRequestDTO fantasyTeamDTO);
}