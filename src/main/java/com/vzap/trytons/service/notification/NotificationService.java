package com.vzap.trytons.service.notification;

import com.vzap.trytons.dto.notification.NotificationCreateRequestDTO;
import com.vzap.trytons.dto.notification.NotificationResponseDTO;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public interface NotificationService {

    List<NotificationResponseDTO> getNotificationsForUser(UUID actorUserId, boolean unreadOnly);
    int getUnreadCount(UUID actorUserId);
    NotificationResponseDTO markAsRead(UUID actorUserId, UUID notificationId);
    int markAllAsRead(UUID actorUserId);
    NotificationResponseDTO createNotification(NotificationCreateRequestDTO request);
    NotificationResponseDTO notifyLeagueMembershipEvent(UUID recipientUserId, UUID leagueId, String body);
    NotificationResponseDTO notifyLeaderboardChange(UUID recipientUserId, UUID leagueId, int newRank);
    NotificationResponseDTO notifyPointsUpdate(UUID recipientUserId, UUID fixtureId, int pointsAwarded);
    NotificationResponseDTO notifySimulatedResult(UUID recipientUserId, UUID fixtureId);
    NotificationResponseDTO notifyPlayerAvailabilityChange(UUID recipientUserId, UUID playerId, String playerName, String newAvailabilityStatus);
    NotificationResponseDTO notifyTransferDeadline(UUID recipientUserId, UUID fixtureId, LocalDateTime deadline);
}
