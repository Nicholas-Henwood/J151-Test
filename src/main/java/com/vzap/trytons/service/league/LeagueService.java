package com.vzap.trytons.service.league;

import com.vzap.trytons.dto.league.JoinLeagueRequestDTO;
import com.vzap.trytons.dto.league.JoinLeagueResponseDTO;
import com.vzap.trytons.dto.league.LeagueMemberResponseDTO;
import com.vzap.trytons.dto.league.LeagueRequestDTO;
import com.vzap.trytons.dto.league.LeagueResponseDTO;
import com.vzap.trytons.dto.publicpreview.PublicLeaguePreviewDTO;

import java.util.List;
import java.util.UUID;

public interface LeagueService {
    LeagueResponseDTO createLeague(LeagueRequestDTO request, UUID currentUserId);
    LeagueResponseDTO getLeague(UUID leagueId,UUID currentUserId);
    boolean isLeagueMember(UUID leagueId, UUID userId);
    List<LeagueResponseDTO> getAllLeagues(UUID currentUserId);

    /**
     * Read-only preview of active PUBLIC leagues for the pre-auth landing page.
     * No user context; exposes only marketing-safe fields. limit &lt;= 0 returns all.
     */
    List<PublicLeaguePreviewDTO> getPublicLeaguePreviews(int limit);
    JoinLeagueResponseDTO joinLeague(JoinLeagueRequestDTO request, UUID currentUserId);
    List<LeagueMemberResponseDTO> listMembers(String actorUserId, String leagueId);
    void removeMember(String actorUserId, String leagueId, String membershipId);
    String getLeagueCode(String actorUserId, String leagueId);
}
