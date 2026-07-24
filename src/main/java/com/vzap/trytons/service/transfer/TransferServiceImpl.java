package com.vzap.trytons.service.transfer;

import com.vzap.trytons.dao.fixture.FantasyRoundDAO;
import com.vzap.trytons.dao.fantasyteam.FantasyTeamDAO;
import com.vzap.trytons.dao.fantasyteam.FantasyTeamPlayerDAO;
import com.vzap.trytons.dao.fixture.FixtureDAO;
import com.vzap.trytons.dao.catalog.PlayerDAO;
import com.vzap.trytons.dao.transfer.TransferDAO;
import com.vzap.trytons.dto.fixture.DeadlineStatusResponseDTO;
import com.vzap.trytons.dto.fixture.LockStatusResponseDTO;
import com.vzap.trytons.dto.fantasyteam.SquadValidationResultDTO;
import com.vzap.trytons.dto.transfer.TransferRequestDTO;
import com.vzap.trytons.dto.transfer.TransferResponseDTO;
import com.vzap.trytons.enums.AvailabilityStatus;
import com.vzap.trytons.enums.FantasyRoundStatus;
import com.vzap.trytons.enums.FixtureStatus;
import com.vzap.trytons.enums.TransferStatus;
import com.vzap.trytons.exceptions.AuthorisationException;
import com.vzap.trytons.exceptions.BusinessRuleException;
import com.vzap.trytons.exceptions.ConflictException;
import com.vzap.trytons.exceptions.DataAccessException;
import com.vzap.trytons.exceptions.ResourceNotFoundException;
import com.vzap.trytons.exceptions.ValidationException;
import com.vzap.trytons.model.fixture.FantasyRound;
import com.vzap.trytons.model.fantasyteam.FantasyTeam;
import com.vzap.trytons.model.fixture.Fixture;
import com.vzap.trytons.model.catalog.Player;
import com.vzap.trytons.model.catalog.PlayerAvailability;
import com.vzap.trytons.model.auth.RegisteredUser;
import com.vzap.trytons.model.fantasyteam.TeamPlayerSelection;
import com.vzap.trytons.model.transfer.Transfer;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import com.vzap.trytons.service.fixture.DeadlineLockService;
import com.vzap.trytons.service.fantasyteam.SquadValidationService;
import com.vzap.trytons.service.notification.NotificationService;

@ApplicationScoped
public class TransferServiceImpl implements TransferService {
    private static final int FREE_TRANSFERS_PER_ROUND = 1;
    private static final int PENALTY_POINTS_PER_EXTRA_TRANSFER = 4;

    @Inject
    private TransferDAO transferDAO;

    @Inject
    private FantasyTeamDAO fantasyTeamDAO;

    @Inject
    private FantasyTeamPlayerDAO fantasyTeamPlayerDAO;

    @Inject
    private PlayerDAO playerDAO;

    @Inject
    private FantasyRoundDAO fantasyRoundDAO;

    @Inject
    private FixtureDAO fixtureDAO;

    @Inject
    private DeadlineLockService deadlineLockService;

    @Inject
    private SquadValidationService squadValidationService;


    @Inject
    private NotificationService notificationService;

    @Override
    @Transactional
    public TransferResponseDTO executeTransfer(String actorUserId, TransferRequestDTO request) {
        validateRequest(actorUserId, request);

        UUID actorId = parseUuid(actorUserId, "Actor user ID is invalid");
        UUID teamId = parseUuid(request.getTeamId(), "Team ID is invalid");
        UUID roundId = parseUuid(request.getRoundId(), "Round ID is invalid");
        UUID removedPlayerId = parseUuid(request.getRemovedPlayerId(), "Removed player ID is invalid");
        UUID addedPlayerId = parseUuid(request.getAddedPlayerId(), "Added player ID is invalid");

        if (removedPlayerId.equals(addedPlayerId)) {
            throw new ValidationException("The same player cannot be both added and removed in one transfer");
        }

        FantasyTeam team = fantasyTeamDAO.getTeamById(teamId).orElseThrow(() -> new ResourceNotFoundException("Fantasy team not found"));

        validateTeamOwnership(actorId, team);

        FantasyRound round = fantasyRoundDAO.getRoundById(roundId).orElseThrow(() -> new ResourceNotFoundException("Fantasy round not found"));

        validateRoundIsOpen(round);
        enforceDeadlineAndLocks(roundId, teamId, removedPlayerId, addedPlayerId);

        if (transferDAO.existsConfirmedTransfer(teamId, roundId, removedPlayerId, addedPlayerId)) {
            throw new ConflictException("This transfer has already been confirmed for this round");
        }

        List<TeamPlayerSelection> currentSquad = fantasyTeamPlayerDAO.getSquadByTeamId(teamId);
        validateCurrentSquadRules(currentSquad, removedPlayerId, addedPlayerId);

        Player removedPlayer = playerDAO.getPlayerById(removedPlayerId).orElseThrow(() -> new ResourceNotFoundException("Removed player not found"));

        Player addedPlayer = playerDAO.getPlayerById(addedPlayerId).orElseThrow(() -> new ResourceNotFoundException("Added player not found"));

        validateIncomingPlayerAvailability(roundId, teamId, addedPlayer);

        BigDecimal removedValue = requirePlayerValue(removedPlayer, "Removed player");
        BigDecimal addedValue = requirePlayerValue(addedPlayer, "Added player");

        BigDecimal totalValue = BigDecimal.ZERO;
        BigDecimal oldRemainingBudget = valueOrZero(team.getRemainingBudget());

        List<TeamPlayerSelection> teamPlayerSelections = fantasyTeamPlayerDAO.getSquadByTeamId(teamId);

        for (TeamPlayerSelection teamPlayerSelection : teamPlayerSelections) {
            Optional<Player> playerOpt = playerDAO.getPlayerById(teamPlayerSelection.getPlayerId());
            if (playerOpt.isPresent()) {
                Player player = playerOpt.get();
                if (player.getValue() != null) {
                    totalValue = totalValue.add(player.getValue());
                }
            }
        }

        BigDecimal oldTeamValue = totalValue;
        BigDecimal newRemainingBudget = oldRemainingBudget.add(removedValue).subtract(addedValue);
        BigDecimal newTeamValue = oldTeamValue.subtract(removedValue).add(addedValue);

        if (newRemainingBudget.compareTo(BigDecimal.ZERO) < 0) {
            throw new BusinessRuleException("You cannot afford this transfer. Insufficient remaining budget.");
        }

        List<UUID> proposedPlayerIds = currentSquad.stream()
                .filter(selection -> selection != null && selection.getPlayerId() != null)
                .map(TeamPlayerSelection::getPlayerId)
                .collect(Collectors.toList());

        proposedPlayerIds.remove(removedPlayerId);
        proposedPlayerIds.add(addedPlayerId);

        SquadValidationResultDTO validationResult = squadValidationService.validateSquad(proposedPlayerIds, List.of(addedPlayerId));

        if (validationResult == null || !validationResult.isValid()) {
            String firstError = "Unknown squad validation error";

            if (validationResult != null
                    && validationResult.getErrors() != null
                    && !validationResult.getErrors().isEmpty()) {
                firstError = validationResult.getErrors().get(0).getMessage();
            }

            throw new BusinessRuleException("Squad validation failed: " + firstError);
        }

        int transfersAlreadyThisRound = transferDAO.countConfirmedTransfers(teamId, roundId);
        boolean penaltyApplies = transfersAlreadyThisRound >= FREE_TRANSFERS_PER_ROUND;
        int penaltyPoints = penaltyApplies ? PENALTY_POINTS_PER_EXTRA_TRANSFER : 0;

        if (penaltyApplies && !request.isPenaltyConfirmed()) {
            throw new BusinessRuleException("This transfer requires penalty confirmation before it can be completed");
        }

        List<TeamPlayerSelection> updatedSquad = currentSquad.stream()
                .map(selection -> {
                    if (selection.getPlayerId().equals(removedPlayerId)) {
                        return TeamPlayerSelection.builder()
                                .selectionId(UUID.randomUUID())
                                .teamId(teamId)
                                .playerId(addedPlayerId)
                                .selectedDate(LocalDateTime.now())
                                .isCaptain(false)
                                .isViceCaptain(false)
                                .squadRole(selection.getSquadRole())
                                .build();
                    }
                    return selection;
                })
                .collect(Collectors.toList());

        fantasyTeamPlayerDAO.replaceSquad(teamId, updatedSquad);

        boolean budgetUpdated = fantasyTeamDAO.updateBudget(teamId,  newRemainingBudget);

        if (!budgetUpdated) {
            throw new DataAccessException("Unable to update budget after transfer", null);
        }

        RegisteredUser createdBy = new RegisteredUser();
        createdBy.setUserId(actorId);

        Transfer transfer = new Transfer();
        transfer.setTransferId(UUID.randomUUID());
        transfer.setTransferDate(LocalDateTime.now());
        transfer.setRoundId(round.getRoundId());
        transfer.setTeamId(team.getTeamId());
        transfer.setRemovedPlayerId(removedPlayer.getPlayerId());
        transfer.setAddedPlayerId(addedPlayer.getPlayerId());
        transfer.setRemovedPlayerName(removedPlayer.getPlayerName());
        transfer.setAddedPlayerName(addedPlayer.getPlayerName());
        transfer.setRemovedPlayerValue(removedValue);
        transfer.setAddedPlayerValue(addedValue);
        transfer.setValueDifference(addedValue.subtract(removedValue));
        transfer.setPenaltyPoints(penaltyPoints);
        transfer.setStatus(TransferStatus.CONFIRMED);
        transfer.setConfirmedAt(LocalDateTime.now());
        transfer.setCreatedByUserId(createdBy.getUserId());
        Transfer savedTransfer = transferDAO.saveTransfer(transfer).orElseThrow(() -> new DataAccessException("Unable to save transfer", null));

        notifyTransferDeadlineReminder(actorId, teamId, round);

        return toResponse(savedTransfer, teamId);
    }

    @Override
    public List<TransferResponseDTO> listTransferHistory(String actorUserId, String teamIdValue) {
        if (isBlank(actorUserId)) {
            throw new AuthorisationException("Authentication is required");
        }

        if (isBlank(teamIdValue)) {
            throw new ValidationException("Team ID is required");
        }

        UUID actorId = parseUuid(actorUserId, "Actor user ID is invalid");
        UUID teamId = parseUuid(teamIdValue, "Team ID is invalid");

        FantasyTeam team = fantasyTeamDAO.getTeamById(teamId)
                .orElseThrow(() -> new ResourceNotFoundException("Fantasy team not found"));

        validateTeamOwnership(actorId, team);

        return transferDAO.getTransfersByTeamId(teamId).stream()
                .map(transfer -> toResponse(transfer, teamId))
                .collect(Collectors.toList());
    }

    private void validateRequest(String actorUserId, TransferRequestDTO request) {
        if (isBlank(actorUserId)) {
            throw new AuthorisationException("Authentication is required");
        }

        if (request == null) {
            throw new ValidationException("Transfer request is required");
        }

        if (isBlank(request.getTeamId())) {
            throw new ValidationException("Team ID is required");
        }

        if (isBlank(request.getRoundId())) {
            throw new ValidationException("Round ID is required");
        }

        if (isBlank(request.getRemovedPlayerId())) {
            throw new ValidationException("Removed player is required");
        }

        if (isBlank(request.getAddedPlayerId())) {
            throw new ValidationException("Added player is required");
        }
    }

    private void validateTeamOwnership(UUID actorId, FantasyTeam team) {
        if (team.getOwnerUserId() == null) {
            throw new BusinessRuleException("Fantasy team owner could not be verified");
        }

        if (!team.getOwnerUserId().equals(actorId)) {
            throw new AuthorisationException("You do not own this fantasy team");
        }
    }

    private void validateTeamIsNotLocked(UUID roundId, UUID teamId) {
        for (Fixture fixture : fixtureDAO.findByRoundId(roundId)) {
            boolean teamIsPlaying = teamId.equals(fixture.getTeamAId()) || teamId.equals(fixture.getTeamBId());

            if (teamIsPlaying && fixture.getStatus() != FixtureStatus.UPCOMING
                    && fixture.getStatus() != FixtureStatus.CANCELLED) {
                throw new BusinessRuleException("This team is locked and cannot make transfers");
            }
        }
    }

    private void validateRoundIsOpen(FantasyRound round) {
        if (round.getStatus() != FantasyRoundStatus.OPEN) {
            throw new BusinessRuleException("Transfers are not open for this round");
        }
    }

    private void enforceDeadlineAndLocks(UUID roundId, UUID teamId, UUID removedPlayerId, UUID addedPlayerId) {
        validateTeamIsNotLocked(roundId, teamId);

        DeadlineStatusResponseDTO deadlineStatus = deadlineLockService.getDeadlineStatus(roundId);

        if (deadlineStatus != null) {
            if (deadlineStatus.isLocked()) {
                throw new BusinessRuleException("The transfer deadline has passed for this round");
            }

            if (!deadlineStatus.isOpenForTransfers()) {
                throw new BusinessRuleException(deadlineStatus.getMessage() != null ? deadlineStatus.getMessage() : "Transfers are not open for this round");
            }
        }

        LockStatusResponseDTO lockStatus = deadlineLockService.getLockStatus(roundId);

        if (lockStatus != null && lockStatus.isLocked()) {
            throw new BusinessRuleException(lockStatus.getMessage() != null ? lockStatus.getMessage() : "This round is locked for transfers");
        }

        List<UUID> lockedTeamIds = deadlineLockService.getLockedTeamIds(roundId);

        if (lockedTeamIds != null && lockedTeamIds.contains(teamId)) {
            throw new BusinessRuleException("This team is locked for the selected round");
        }

        List<UUID> lockedPlayerIds = deadlineLockService.getLockedPlayerIds(roundId, teamId);

        if (lockedPlayerIds != null && lockedPlayerIds.contains(removedPlayerId)) {
            throw new BusinessRuleException("The player you are trying to remove is locked");
        }

        if (lockedPlayerIds != null && lockedPlayerIds.contains(addedPlayerId)) {
            throw new BusinessRuleException("The player you are trying to add is locked");
        }
    }

    private void validateCurrentSquadRules(
            List<TeamPlayerSelection> currentSquad,
            UUID removedPlayerId,
            UUID addedPlayerId) {
        if (currentSquad == null) {
            throw new BusinessRuleException("Current squad could not be loaded");
        }

        boolean removedPlayerInSquad = currentSquad.stream()
                .filter(selection -> selection != null && selection.getPlayerId() != null)
                .anyMatch(selection -> removedPlayerId.equals(selection.getPlayerId()));

        if (!removedPlayerInSquad) {
            throw new BusinessRuleException("The player you are trying to remove is not in your squad");
        }
        boolean addedPlayerAlreadyInSquad = currentSquad.stream()
                .filter(selection -> selection != null && selection.getPlayerId() != null)
                .anyMatch(selection -> addedPlayerId.equals(selection.getPlayerId()));

        if (addedPlayerAlreadyInSquad) {
            throw new BusinessRuleException("The player you are trying to add is already in your squad");
        }
    }

    private void validateIncomingPlayerAvailability(UUID roundId, UUID teamId, Player addedPlayer) {
        if (!addedPlayer.isActive()) {
            throw new BusinessRuleException("The player you are trying to add is not active");
        }

        PlayerAvailability availability = playerDAO.getCurrentAvailability(addedPlayer.getPlayerId()).orElse(null);

        if (availability != null && availability.getStatus() != AvailabilityStatus.ACTIVE) {
            throw new BusinessRuleException("The player you are trying to add is not available");
        }

        List<Player> availablePlayers = deadlineLockService.getAvailableTransferPlayers(roundId, teamId);

        if (availablePlayers != null) {
            boolean playerAvailableForRound = availablePlayers.stream()
                    .filter(player -> player != null && player.getPlayerId() != null)
                    .anyMatch(player -> addedPlayer.getPlayerId().equals(player.getPlayerId()));

            if (!playerAvailableForRound) {
                throw new BusinessRuleException("The player you are trying to add is not available for this round");
            }
        }
    }

    private BigDecimal requirePlayerValue(Player player, String label) {
        if (player.getValue() == null) {
            throw new BusinessRuleException(label + " has no value set, so the transfer cannot be processed");
        }

        return player.getValue();
    }

    private TransferResponseDTO toResponse(Transfer transfer, UUID fallbackTeamId) {
        BigDecimal valueDifference = transfer.getValueDifference();
        if (valueDifference == null && transfer.getAddedPlayerValue() != null && transfer.getRemovedPlayerValue() != null) {
            valueDifference = transfer.getAddedPlayerValue().subtract(transfer.getRemovedPlayerValue());
        }
        return TransferResponseDTO.builder()
                .transferId(transfer.getTransferId())
                .teamId(transfer.getTeamId() != null ? transfer.getTeamId() : fallbackTeamId)
                .roundId(transfer.getRoundId() != null ? transfer.getRoundId() : null)
                .removedPlayerId(transfer.getRemovedPlayerId() != null ? transfer.getRemovedPlayerId() : null)
                .addedPlayerId(transfer.getAddedPlayerId() != null ? transfer.getAddedPlayerId() : null)
                .removedPlayerName(transfer.getRemovedPlayerName())
                .addedPlayerName(transfer.getAddedPlayerName())
                .removedPlayerValue(transfer.getRemovedPlayerValue())
                .addedPlayerValue(transfer.getAddedPlayerValue())
                .valueDifference(valueDifference)
                .penaltyPoints(transfer.getPenaltyPoints())
                .status(transfer.getStatus() != null ? transfer.getStatus().name() : null)
                .transferDate(transfer.getTransferDate())
                .confirmationDate(transfer.getConfirmedAt())
                .build();
    }

    private UUID parseUuid(String value, String message) {
        try {
            return UUID.fromString(value);
        } catch (IllegalArgumentException e) {
            throw new ValidationException(message);
        }
    }

    private BigDecimal valueOrZero(BigDecimal value) {
        return value != null ? value : BigDecimal.ZERO;
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    private void notifyTransferDeadlineReminder(UUID actorId, UUID teamId, FantasyRound round) {
        try {
            UUID fixtureId = findFixtureIdForTeamInRound(round.getRoundId(), teamId);
            if (fixtureId != null) {
                notificationService.notifyTransferDeadline(actorId, fixtureId, round.getLockDeadline());
            }
        } catch (Exception e) {
            // Notification failure must not affect a confirmed transfer
        }
    }

    private UUID findFixtureIdForTeamInRound(UUID roundId, UUID teamId) {
        for (Fixture fixture : fixtureDAO.findByRoundId(roundId)) {
            if (teamId.equals(fixture.getTeamAId()) || teamId.equals(fixture.getTeamBId())) {
                return fixture.getFixtureId();
            }
        }
        return null;
    }
}