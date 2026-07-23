package com.vzap.trytons.service.catalog;

import com.vzap.trytons.dao.auth.UserDAO;
import com.vzap.trytons.dao.catalog.PlayerAvailabilityDAO;
import com.vzap.trytons.dao.catalog.PlayerDAO;
import com.vzap.trytons.dao.fantasyteam.FantasyTeamDAO;
import com.vzap.trytons.dao.fantasyteam.FantasyTeamPlayerDAO;
import com.vzap.trytons.dto.catalog.PlayerAvailabilityRequestDTO;
import com.vzap.trytons.dto.catalog.PlayerAvailabilityResponseDTO;
import com.vzap.trytons.enums.UserRole;
import com.vzap.trytons.exceptions.AuthorisationException;
import com.vzap.trytons.exceptions.ResourceNotFoundException;
import com.vzap.trytons.exceptions.ValidationException;
import com.vzap.trytons.model.auth.User;
import com.vzap.trytons.model.catalog.Player;
import com.vzap.trytons.model.catalog.PlayerAvailability;
import com.vzap.trytons.model.fantasyteam.FantasyTeam;
import com.vzap.trytons.service.notification.NotificationService;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

@ApplicationScoped
public class PlayerAvailabilityServiceImpl implements PlayerAvailabilityService {
    private static final Logger LOG = Logger.getLogger(PlayerAvailabilityServiceImpl.class.getName());

    @Inject
    private PlayerAvailabilityDAO playerAvailabilityDAO;

    @Inject
    private PlayerDAO playerDAO;

    @Inject
    private UserDAO userDAO;

    @Inject
    private FantasyTeamPlayerDAO fantasyTeamPlayerDAO;

    @Inject
    private FantasyTeamDAO fantasyTeamDAO;

    @Inject
    private NotificationService notificationService;

    @Override
    public PlayerAvailabilityResponseDTO setAvailability(UUID actorUserId, UUID playerId, PlayerAvailabilityRequestDTO request) {
        requireAdmin(actorUserId);
        validatePlayerId(playerId);
        validateRequest(request);

        Player player = playerDAO.getPlayerById(playerId).orElseThrow(() -> new ResourceNotFoundException("Player was not found."));

        PlayerAvailability availability = mapRequestToModel(playerId, request);
        PlayerAvailability saved = playerAvailabilityDAO.upsert(availability);

        notifyAffectedTeamOwners(player, saved);

        return mapToResponse(saved);
    }

    private void notifyAffectedTeamOwners(Player player, PlayerAvailability saved) {
        try {
            String status = saved.getStatus() != null ? saved.getStatus().name() : null;
            List<UUID> teamIds = fantasyTeamPlayerDAO.getTeamIdsByPlayerId(player.getPlayerId());
            for (UUID teamId : teamIds) {
                fantasyTeamDAO.getTeamById(teamId)
                        .map(FantasyTeam::getOwnerUserId)
                        .ifPresent(ownerId -> notificationService.notifyPlayerAvailabilityChange(ownerId, player.getPlayerId(), player.getPlayerName(), status));
            }
        } catch (Exception e) {
            LOG.log(Level.WARNING, "Failed to send player-availability-change notifications for player " + player.getPlayerId(), e);
        }
    }

    @Override
    public Optional<PlayerAvailabilityResponseDTO> getAvailability(UUID playerId) {
        validatePlayerId(playerId);

        return playerAvailabilityDAO.getCurrentByPlayer(playerId).map(this::mapToResponse);
    }

    private void requireAdmin(UUID actorUserId) {
        if (actorUserId == null) {
            throw new ValidationException("An authenticated administrator is required.");
        }

        Optional<User> userOptional = userDAO.getUserById(actorUserId);

        if (userOptional.isEmpty()) {
            throw new AuthorisationException("An authenticated administrator is required.");
        }

        User user = userOptional.get();

        if (user.getRole() != UserRole.ADMINISTRATOR) {
            throw new AuthorisationException("Only admins can perform this action.");
        }
    }

    private void validatePlayerId(UUID playerId) {
        if (playerId == null) {
            throw new ValidationException("Player ID is required.");
        }
    }

    private void validateRequest(PlayerAvailabilityRequestDTO request) {
        if (request == null) {
            throw new ValidationException("Availability details are required.");
        }

        if (request.getStatus() == null) {
            throw new ValidationException("Availability status is required.");
        }

        if (request.getEffectiveDate() == null) {
            throw new ValidationException("Effective date is required.");
        }

        if (request.getEndDate() != null && request.getEndDate().isBefore(request.getEffectiveDate())) {
            throw new ValidationException("End date cannot be before the effective date.");
        }
    }

    private PlayerAvailability mapRequestToModel(UUID playerId, PlayerAvailabilityRequestDTO request) {
        PlayerAvailability availability = new PlayerAvailability();

        availability.setPlayerId(playerId);
        availability.setStatus(request.getStatus());
        availability.setEffectiveDate(request.getEffectiveDate());
        availability.setEndDate(request.getEndDate());
        availability.setNotes(request.getNotes() != null ? request.getNotes().trim() : null);

        return availability;
    }

    private PlayerAvailabilityResponseDTO mapToResponse(PlayerAvailability availability) {
        PlayerAvailabilityResponseDTO response = new PlayerAvailabilityResponseDTO();

        response.setAvailabilityId(availability.getAvailabilityId());
        response.setPlayerId(availability.getPlayerId());
        response.setStatus(availability.getStatus());
        response.setEffectiveDate(availability.getEffectiveDate());
        response.setEndDate(availability.getEndDate());
        response.setNotes(availability.getNotes());

        return response;
    }
}
