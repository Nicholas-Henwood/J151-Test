package com.vzap.trytons.dao.notification;

import com.vzap.trytons.enums.NotificationType;
import com.vzap.trytons.exceptions.DataAccessException;
import com.vzap.trytons.model.notification.Notification;
import jakarta.ejb.Singleton;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import com.vzap.trytons.dao.shared.BaseDAO;

@Singleton
public class NotificationDAOImpl extends BaseDAO implements NotificationDAO {

    private static final Logger LOG = Logger.getLogger(NotificationDAOImpl.class.getName());

    private static Notification mapRow(ResultSet rs)  {
        try {
            Notification notification = new Notification();
            notification.setNotificationId(UUID.fromString(rs.getString("notificationId")));
            notification.setType(NotificationType.valueOf(rs.getString("type")));
            notification.setBody(rs.getString("body"));
            notification.setCreatedAt(rs.getTimestamp("createdAt").toLocalDateTime());
            notification.setIsRead(rs.getBoolean("isRead"));
            notification.setRelatedEntityType(rs.getString("related_entity_type"));
            String relatedEntityId = rs.getString("related_entity_id");
            notification.setRelatedEntityId(relatedEntityId != null ? UUID.fromString(relatedEntityId) : null);
            String userId = rs.getString("userId");
            notification.setUserId(userId != null ? UUID.fromString(userId) : null);
            return notification;
        }catch(SQLException e){
           throw new DataAccessException(e.getMessage(), e);
        }
    }

    @Override
    public Notification create(Notification notification) {
        UUID newId = UUID.randomUUID();
        String query = "INSERT INTO notification "
                + "(notificationId, userId, type, body, isRead, related_entity_type, related_entity_id) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?)";

        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement(query)) {

            ps.setString(1, newId.toString());
            ps.setString(2, notification.getUserId().toString());
            ps.setString(3, notification.getType().name());
            ps.setString(4, notification.getBody());
            ps.setBoolean(5, Boolean.TRUE.equals(notification.getIsRead()));
            ps.setString(6, notification.getRelatedEntityType());
            ps.setString(7, notification.getRelatedEntityId() != null ? notification.getRelatedEntityId().toString() : null);

            if (!(ps.executeUpdate() > 0)) {
                throw new SQLException();
            }

            notification.setNotificationId(newId);
            return notification;

        } catch (SQLException e) {
            LOG.log(Level.SEVERE, "Unable to create notification", e);
            throw new DataAccessException("Unable to create notification", e);
        }
    }

    @Override
    public Optional<Notification> findById(UUID notificationId) {
        String query = "SELECT * FROM notification WHERE notificationId = ?";
        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement(query)) {

            ps.setString(1, notificationId.toString());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            LOG.log(Level.SEVERE, "Unable to find notification by ID", e);
            throw new DataAccessException("Unable to find notification by ID", e);
        }
        return Optional.empty();
    }

    @Override
    public List<Notification> findByUserId(UUID userId) {
        String query = "SELECT * FROM notification WHERE userId = ? ORDER BY createdAt DESC";
        List<Notification> notifications = new ArrayList<>();
        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement(query)) {

            ps.setString(1, userId.toString());
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    notifications.add(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            LOG.log(Level.SEVERE, "Unable to find notifications for user", e);
            throw new DataAccessException("Unable to find notifications for user", e);
        }
        return notifications;
    }

    @Override
    public List<Notification> findByUserIdAndReadStatus(UUID userId, boolean isRead) {
        String query = "SELECT * FROM notification WHERE userId = ? AND isRead = ? ORDER BY createdAt DESC";
        List<Notification> notifications = new ArrayList<>();
        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement(query)) {

            ps.setString(1, userId.toString());
            ps.setBoolean(2, isRead);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    notifications.add(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            LOG.log(Level.SEVERE, "Unable to find notifications by read status", e);
            throw new DataAccessException("Unable to find notifications by read status", e);
        }
        return notifications;
    }

    @Override
    public int countUnreadByUserId(UUID userId) {
        String query = "SELECT COUNT(*) AS unreadCount FROM notification WHERE userId = ? AND isRead = FALSE";
        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement(query)) {

            ps.setString(1, userId.toString());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("unreadCount");
                }
            }
        } catch (SQLException e) {
            LOG.log(Level.SEVERE, "Unable to count unread notifications", e);
            throw new DataAccessException("Unable to count unread notifications", e);
        }
        return 0;
    }

    @Override
    public boolean markAsRead(UUID notificationId) {
        String query = "UPDATE notification SET isRead = TRUE WHERE notificationId = ?";
        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement(query)) {

            ps.setString(1, notificationId.toString());
            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            LOG.log(Level.SEVERE, "Unable to mark notification as read", e);
            throw new DataAccessException("Unable to mark notification as read", e);
        }
    }

    @Override
    public int markAllAsReadForUser(UUID userId) {
        String query = "UPDATE notification SET isRead = TRUE WHERE userId = ? AND isRead = FALSE";
        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement(query)) {

            ps.setString(1, userId.toString());
            return ps.executeUpdate();

        } catch (SQLException e) {
            LOG.log(Level.SEVERE, "Unable to mark all notifications as read", e);
            throw new DataAccessException("Unable to mark all notifications as read", e);
        }
    }
}