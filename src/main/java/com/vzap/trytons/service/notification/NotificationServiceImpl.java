package com.vzap.trytons.service.notification;

import com.vzap.trytons.dao.notification.NotificationDAO;
import com.vzap.trytons.dao.auth.UserDAO;
import com.vzap.trytons.dto.notification.NotificationCreateRequestDTO;
import com.vzap.trytons.dto.notification.NotificationResponseDTO;
import com.vzap.trytons.enums.NotificationType;
import com.vzap.trytons.exceptions.AuthorisationException;
import com.vzap.trytons.exceptions.ResourceNotFoundException;
import com.vzap.trytons.exceptions.ValidationException;
import com.vzap.trytons.model.notification.Notification;
import com.vzap.trytons.model.auth.User;
import jakarta.inject.Inject;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class NotificationServiceImpl implements NotificationService{

    @Inject
    private NotificationDAO notificationDAO;
    @Inject
    private UserDAO userDAO;

    @Override
    public List<NotificationResponseDTO> getNotificationsForUser(UUID actorUserId, boolean unreadOnly) {
        requireAuthenticated(actorUserId);

        List<Notification> notifications;
        if(Boolean.TRUE.equals(unreadOnly)){
            notifications = notificationDAO.findByUserIdAndReadStatus(actorUserId, false);
        }else{
            notifications = notificationDAO.findByUserId(actorUserId);
        }

        List<NotificationResponseDTO> response = new ArrayList<>();
        for(Notification notification : notifications){
            response.add(mapToResponse(notification));
        }
        return response;
    }

    @Override
    public int getUnreadCount(UUID actorUserId) {
        requireAuthenticated(actorUserId);
        return notificationDAO.countUnreadByUserId(actorUserId);
    }

    @Override
    public NotificationResponseDTO markAsRead(UUID actorUserId, UUID notificationId) {
        requireAuthenticated(actorUserId);

        if(notificationId == null){
            throw new ValidationException("No Notification Id was found");
        }

        Notification notification = notificationDAO.findById(notificationId).orElseThrow(() -> new ResourceNotFoundException("Notification not found"));

        if(notification.getUserId() == null || !actorUserId.equals(notification.getUserId())){
            throw new AuthorisationException("User may only mark their own messages as read");
        }

        notificationDAO.markAsRead(notificationId);
        notification.setIsRead(true);
        return mapToResponse(notification);
    }

    @Override
    public int markAllAsRead(UUID actorUserId) {
        requireAuthenticated(actorUserId);
        return notificationDAO.markAllAsReadForUser(actorUserId);
    }

    @Override
    public NotificationResponseDTO createNotification(NotificationCreateRequestDTO request) {
        if (request == null) {
            throw new ValidationException("Notification details are required.");
        }
        if (request.getUserId() == null) {
            throw new ValidationException("A target user is required.");
        }
        if (request.getType() == null) {
            throw new ValidationException("A notification type is required.");
        }
        if (request.getBody() == null || request.getBody().isBlank()) {
            throw new ValidationException("Notification body is required.");
        }
        if ((request.getRelatedEntityType() == null) != (request.getRelatedEntityId() == null)) {
            throw new ValidationException("Related entity type and related entity ID must be provided together to continue.");
        }

        userDAO.getUserById(request.getUserId()).orElseThrow(() -> new ResourceNotFoundException("Target user not found."));

        Notification notification = new Notification();
        notification.setUserId(request.getUserId());
        notification.setType(request.getType());
        notification.setBody(request.getBody());
        notification.setIsRead(false);
        notification.setRelatedEntityType(request.getRelatedEntityType());
        notification.setRelatedEntityId(request.getRelatedEntityId());
        Notification created = notificationDAO.create(notification);
        return mapToResponse(created);
    }

    @Override
    public NotificationResponseDTO notifyLeagueMembershipEvent(UUID recipientUserId, UUID leagueId, String body) {
        return createNotification(buildRequest(recipientUserId, NotificationType.LEAGUE_INVITATION, body, "LEAGUE", leagueId));
    }

    @Override
    public NotificationResponseDTO notifyLeaderboardChange(UUID recipientUserId, UUID leagueId, int newRank) {
        String body = "Your league standing has changed! You are now ranked #" + newRank + ".";
        return createNotification(buildRequest(recipientUserId, NotificationType.LEADERBOARD_CHANGE, body, "LEAGUE", leagueId));
    }

    @Override
    public NotificationResponseDTO notifyPointsUpdate(UUID recipientUserId, UUID fixtureId, int pointsAwarded) {
        String body = "Your team earned " + pointsAwarded + " points from the latest game!";
        return createNotification(buildRequest(recipientUserId, NotificationType.POINTS_UPDATE, body, "FIXTURE", fixtureId));
    }

    @Override
    public NotificationResponseDTO notifySimulatedResult(UUID recipientUserId, UUID fixtureId) {
        String body = "The match is complete and the results are in!";
        return createNotification(buildRequest(recipientUserId, NotificationType.SIMULATED_RESULT, body, "FIXTURE", fixtureId));
    }

    @Override
    public NotificationResponseDTO notifyPlayerAvailabilityChange(UUID recipientUserId, UUID playerId, String playerName, String newAvailabilityStatus) {
        String name = (playerName == null || playerName.isBlank()) ? "Player" : playerName;
        String body = name + " is now " + newAvailabilityStatus + ".";
        return createNotification(buildRequest(recipientUserId, NotificationType.PLAYER_AVAILABILITY, body, "PLAYER", playerId));
    }

    @Override
    public NotificationResponseDTO notifyTransferDeadline(UUID recipientUserId, UUID fixtureId, LocalDateTime deadline) {
        String body = (deadline == null) ? "The deadline for transfers is almost here for your team" : "Transfers close at " + deadline + " for this upcoming fixture";

        return createNotification(buildRequest(recipientUserId, NotificationType.TRANSFER_DEADLINE, body, "FIXTURE", fixtureId));
    }

    private NotificationCreateRequestDTO buildRequest(UUID userId, NotificationType type, String body, String relatedEntityType, UUID relatedEntityId) {
        NotificationCreateRequestDTO request = new NotificationCreateRequestDTO();
        request.setUserId(userId);
        request.setType(type);
        request.setBody(body);
        request.setRelatedEntityType(relatedEntityType);
        request.setRelatedEntityId(relatedEntityId);

        return request;
    }

    private void requireAuthenticated(UUID actorUserId) {
        if (actorUserId == null) {
            throw new AuthorisationException("An authenticated user is required.");
        }
        Optional<User> user = userDAO.getUserById(actorUserId);
        if (user.isEmpty() || !Boolean.TRUE.equals(user.get().getIsActive())) {
            throw new AuthorisationException("An authenticated user is required.");
        }
    }

    private NotificationResponseDTO mapToResponse(Notification notification) {

        return NotificationResponseDTO.builder()
                .notificationId(notification.getNotificationId())
                .type(notification.getType())
                .body(notification.getBody())
                .createdAt(notification.getCreatedAt())
                .isRead(Boolean.TRUE.equals(notification.getIsRead()))
                .relatedEntityType(notification.getRelatedEntityType())
                .relatedEntityId(notification.getRelatedEntityId())
                .build();
    }
}
