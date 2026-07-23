package com.vzap.trytons.dao.admin;

import com.vzap.trytons.dao.shared.BaseDAO;
import com.vzap.trytons.exceptions.DataAccessException;
import com.vzap.trytons.model.admin.Log;
import com.vzap.trytons.model.admin.LogActionCount;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

public class LogDAOImpl extends BaseDAO implements LogDAO {
    private static final Logger LOG = Logger.getLogger(LogDAOImpl.class.getName());
    private Log mapRow(ResultSet rs) throws SQLException {
        String userId = rs.getString("userId");
        String transferId = rs.getString("transferId");
        String notificationId = rs.getString("notificationId");
        String entityId = rs.getString("entityId");
        Timestamp createdAtTimestamp = rs.getTimestamp("createdAt");

        Log log = new Log();
        log.setLogId(UUID.fromString(rs.getString("logId")));
        log.setUserId(userId == null ? null : UUID.fromString(userId));
        log.setTransferId(transferId == null ? null : UUID.fromString(transferId));
        log.setNotificationId(notificationId == null ? null : UUID.fromString(notificationId));
        log.setEntityType(rs.getString("entityType"));
        log.setEntityId(entityId == null ? null : UUID.fromString(entityId));
        log.setActionType(rs.getString("actionType"));
        log.setDescription(rs.getString("description"));
        log.setCreatedAt(createdAtTimestamp == null ? null : createdAtTimestamp.toLocalDateTime());
        log.setIpAddress(rs.getString("ipAddress"));
        return log;
    }
    @Override
    public List<Log> findRecentLogs(int limit) {
        String query = """
                SELECT logId, userId, transferId, notificationId, entityType, entityId, actionType, description, createdAt, ipAddress
                FROM log
                ORDER BY createdAt DESC
                LIMIT ?""";
        try(Connection con = getConnection();
            PreparedStatement ps = con.prepareStatement(query)){
            ps.setInt(1, limit);
            try(ResultSet rs = ps.executeQuery()){
                List<Log> logs = new ArrayList<>();
                while (rs.next()) {
                    logs.add(mapRow(rs));
                }
                return logs;
            }
        }catch(SQLException e){
            LOG.log(Level.SEVERE, "Unable to find the recent logs", e);
            throw new DataAccessException("Unable to find the recent logs", e);
        }
    }

    @Override
    public List<LogActionCount> countByActionType() {
        String query ="SELECT actionType, COUNT(*) AS actionCount"+
                "FROM log"+
                "GROUP BY actionType" +
                "ORDER BY actionCount DESC";

        try(Connection con = getConnection();
        PreparedStatement ps = con.prepareStatement(query);
        ResultSet rs = ps.executeQuery()){
            List<LogActionCount> count = new ArrayList<>();
            while (rs.next()) {
                count.add( new LogActionCount(rs.getString("actionType"), rs.getInt("actionCount")));
            }
            return count;
        }catch(SQLException e){
            LOG.log(Level.SEVERE, "Unable to count by action type", e);
            throw new DataAccessException("Unable to count by action type", e);
        }
    }
}
