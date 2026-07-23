package com.vzap.trytons.dao.notification;

import com.vzap.trytons.model.notification.Notification;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface NotificationDAO {

    Notification create(Notification notification);
    Optional<Notification> findById(UUID notificationId);
    List<Notification> findByUserId(UUID userId);
    List<Notification> findByUserIdAndReadStatus(UUID userId, boolean read);
    int countUnreadByUserId(UUID userId);
    boolean markAsRead(UUID notificationId);
    int markAllAsReadForUser(UUID userId);
}
