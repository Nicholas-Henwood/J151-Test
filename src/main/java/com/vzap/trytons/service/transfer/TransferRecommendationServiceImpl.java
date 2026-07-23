package com.vzap.trytons.service.transfer;

import com.vzap.trytons.dao.catalog.ClubDAO;
import com.vzap.trytons.dao.fantasyteam.FantasyTeamDAO;
import com.vzap.trytons.dao.fantasyteam.FantasyTeamPlayerDAO;
import com.vzap.trytons.dao.catalog.PlayerDAO;
import com.vzap.trytons.dao.catalog.PositionDAO;
import com.vzap.trytons.dto.transfer.RecommendedPlayerDTO;
import com.vzap.trytons.dto.transfer.TransferRecommendationRequestDTO;
import com.vzap.trytons.dto.transfer.TransferRecommendationResponseDTO;
import com.vzap.trytons.enums.AvailabilityStatus;
import com.vzap.trytons.exceptions.AuthenticationException;
import com.vzap.trytons.exceptions.AuthorisationException;
import com.vzap.trytons.exceptions.ResourceNotFoundException;
import com.vzap.trytons.exceptions.ValidationException;
import com.vzap.trytons.model.fantasyteam.FantasyTeam;
import com.vzap.trytons.model.catalog.Player;
import com.vzap.trytons.model.fantasyteam.TeamPlayerSelection;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@ApplicationScoped
public class TransferRecommendationServiceImpl implements TransferRecommendationService {
    private static final int CANDIDATES_PER_PLAYER = 3;
    private static final int MAX_TOTAL_RECOMMENDATIONS = 10;

    @Inject
    private FantasyTeamDAO fantasyTeamDAO;

    @Inject
    private FantasyTeamPlayerDAO fantasyTeamPlayerDAO;

    @Inject
    private PlayerDAO playerDAO;

    @Inject
    private ClubDAO clubDAO;

    @Inject
    private PositionDAO positionDAO;

    @Override
    public TransferRecommendationResponseDTO recommendTransfers(UUID actorUserId, TransferRecommendationRequestDTO request) {
        validateRequest(actorUserId, request);

        FantasyTeam team = fantasyTeamDAO.getTeamById(request.getTeamId()).orElseThrow(() -> new ResourceNotFoundException("Fantasy team not found"));

        if (!actorUserId.equals(team.getOwnerUserId())) {
            throw new AuthorisationException("You do not own this fantasy team");
        }

        List<TeamPlayerSelection> currentSquad = fantasyTeamPlayerDAO.getSquadByTeamId(request.getTeamId());
        if (currentSquad.isEmpty()) {
            if (request.getCurrentPlayerId() != null) {
                throw new ResourceNotFoundException("Player not found in the squad");
            }
            return TransferRecommendationResponseDTO.builder()
                    .teamId(team.getTeamId())
                    .recommendations(List.of())
                    .build();
        }

        List<Player> squadPlayers = loadSquadPlayers(currentSquad);

        Set<UUID> squadPlayerIds = getSquadPlayerIds(squadPlayers);

        List<Player> focusPlayers = getFocusPlayers(squadPlayers, request.getCurrentPlayerId());

        List<RecommendedPlayerDTO> recommendations = new ArrayList<>();

        for (Player outgoing : focusPlayers) {
            recommendations.addAll(findReplacementsFor(outgoing, team, squadPlayerIds));
        }

        recommendations.sort(Comparator.comparingInt(RecommendedPlayerDTO::getCurrentForm).reversed());

        if (recommendations.size() > MAX_TOTAL_RECOMMENDATIONS) {
            recommendations = new ArrayList<>(recommendations.subList(0, MAX_TOTAL_RECOMMENDATIONS));
        }

        return TransferRecommendationResponseDTO.builder()
                .teamId(team.getTeamId())
                .recommendations(recommendations)
                .build();
    }

    private void validateRequest(UUID actorUserId, TransferRecommendationRequestDTO request) {
        if (actorUserId == null) {
            throw new AuthenticationException("Authentication required");
        }

        if (request == null) {
            throw new ValidationException("Transfer recommendation request is required");
        }

        if (request.getTeamId() == null) {
            throw new ValidationException("Fantasy team ID is required");
        }
    }

    private List<Player> loadSquadPlayers(List<TeamPlayerSelection> currentSquad) {
        List<Player> squadPlayers = new ArrayList<>();

        for (TeamPlayerSelection selection : currentSquad) {
            if (selection == null || selection.getPlayerId() == null) {
                throw new ResourceNotFoundException("Player not found");
            }

            Player player = playerDAO.getPlayerById(selection.getPlayerId()).orElseThrow(() -> new ResourceNotFoundException("Player not found"));

            squadPlayers.add(player);
        }
        return squadPlayers;
    }

    private Set<UUID> getSquadPlayerIds(List<Player> squadPlayers) {
        Set<UUID> squadPlayerIds = new HashSet<>();

        for (Player player : squadPlayers) {
            squadPlayerIds.add(player.getPlayerId());
        }
        return squadPlayerIds;
    }

    private List<Player> getFocusPlayers(List<Player> squadPlayers, UUID currentPlayerId) {
        if (currentPlayerId == null) {
            return squadPlayers;
        }
        for (Player player : squadPlayers) {
            if (currentPlayerId.equals(player.getPlayerId())) {
                return List.of(player);
            }
        }
        throw new ResourceNotFoundException("Player not found in the squad");
    }

    private List<RecommendedPlayerDTO> findReplacementsFor(Player outgoing, FantasyTeam team, Set<UUID> squadPlayerIds) {
        BigDecimal affordableBudget = team.getRemainingBudget().add(outgoing.getValue());

        List<Player> candidates = playerDAO.searchPlayers(null, null, outgoing.getPositionId(), null, affordableBudget, null, null, AvailabilityStatus.ACTIVE, true);

        candidates.sort(Comparator.comparingInt(Player::getCurrentForm)
                .reversed()
                .thenComparing(Player::getValue));

        List<RecommendedPlayerDTO> recommendations = new ArrayList<>();

        for (Player candidate : candidates) {
            if (candidate.getPlayerId().equals(outgoing.getPlayerId())) {
                continue;
            }

            if (squadPlayerIds.contains(candidate.getPlayerId())) {
                continue;
            }

            recommendations.add(toRecommendedPlayerDTO(candidate, outgoing));

            if (recommendations.size() == CANDIDATES_PER_PLAYER) {
                break;
            }
        }
        return recommendations;
    }

    private RecommendedPlayerDTO toRecommendedPlayerDTO(Player candidate, Player outgoing) {
        String positionName = getPositionName(candidate.getPositionId());
        String clubName = getClubName(candidate.getClubId());

        return RecommendedPlayerDTO.builder()
                .playerId(candidate.getPlayerId())
                .playerName(candidate.getPlayerName())
                .positionName(positionName)
                .clubName(clubName)
                .value(candidate.getValue())
                .currentForm(candidate.getCurrentForm())
                .availabilityStatus(AvailabilityStatus.ACTIVE.name())
                .replacesPlayerId(outgoing.getPlayerId())
                .reason(buildReason(candidate, outgoing))
                .build();
    }

    private String getPositionName(UUID positionId) {
        if (positionId == null) {
            throw new ResourceNotFoundException("Position not found");
        }

        return positionDAO.findById(positionId)
                .orElseThrow(() -> new ResourceNotFoundException("Position not found"))
                .getPositionName();
    }

    private String getClubName(UUID clubId) {
        if (clubId == null) {
            throw new ResourceNotFoundException("Club not found");
        }

        return clubDAO.findByClubId(clubId).orElseThrow(() -> new ResourceNotFoundException("Club not found")).getClubName();
    }

    private String buildReason(Player candidate, Player outgoing) {
        if (candidate.getCurrentForm() > outgoing.getCurrentForm()) {
            return "Better current form (" + candidate.getCurrentForm() + " vs " + outgoing.getCurrentForm() + ") in the same position";
        }

        if (candidate.getValue().compareTo(outgoing.getValue()) < 0) {
            return "Comparable ability at a lower price, freeing up budget";
        }

        return "Available same-position option within your budget";
    }
}