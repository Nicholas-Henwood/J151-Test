package com.vzap.trytons.dao.fixture;

import com.vzap.trytons.enums.RoundLockAction;
import com.vzap.trytons.exceptions.DataAccessException;
import com.vzap.trytons.model.fixture.RoundLock;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import com.vzap.trytons.dao.shared.BaseDAO;

public class RoundLockDAOImpl extends BaseDAO implements RoundLockDAO {
    private static final Logger LOG = Logger.getLogger(RoundLockDAOImpl.class.getName());
    @Override
    public Optional<RoundLock> createRoundLock(RoundLock roundLock) {
    String query =  "INSERT into roundLock (lockId, roundId, lockAction, action_by_admin_user_id, actionAt,reason) VALUES (?, ?, ?, ?, ?, ?)";
    UUID newLockId = UUID.randomUUID();
    try(Connection con = getConnection();
        PreparedStatement ps = con.prepareStatement(query)){
        ps.setString(1, newLockId.toString());
        ps.setString(2,roundLock.getRoundId().toString());
        ps.setString(3,roundLock.getLockAction().toString());
        if(roundLock.getActionByAdminUserId() == null){
            ps.setNull(4, Types.VARCHAR);
        }else{
            ps.setString(4, roundLock.getActionByAdminUserId().toString());
        }
        ps.setTimestamp(5,Timestamp.valueOf(roundLock.getActionAt()));
        ps.setString(6, roundLock.getReason());
        if(ps.executeUpdate() >0){
            RoundLock lock = new RoundLock();
            lock.setLockId(newLockId);
            lock.setRoundId(roundLock.getRoundId());
            lock.setLockAction(roundLock.getLockAction());
            lock.setActionByAdminUserId(roundLock.getActionByAdminUserId());
            lock.setActionAt(roundLock.getActionAt());
            lock.setReason(roundLock.getReason());
            return Optional.of(lock);
        }
    }catch(SQLException e){
        LOG.log(Level.SEVERE,"Unable to make a round lock",e);
        throw new DataAccessException("Unable to make a round lock",e);
    }
        return Optional.empty();
    }

    @Override
    public Optional<RoundLock> getRoundLockById(UUID lockId) {
        String query = "SELECT * FROM roundLock WHERE lockId = ?";
        try(Connection con = getConnection();
        PreparedStatement ps = con.prepareStatement(query)){
            ps.setString(1, lockId.toString());
            try(ResultSet rs = ps.executeQuery()){
                if(rs.next()){
                    String adminIdStr = rs.getString("action_by_admin_user_id");
                    UUID adminId = (adminIdStr != null) ? UUID.fromString(adminIdStr) : null;
                    RoundLock rl = RoundLock.builder()
                            .lockId(lockId)
                            .roundId(UUID.fromString(rs.getString("roundId")))
                            .lockAction(RoundLockAction.valueOf(rs.getString("lockAction")))
                            .actionByAdminUserId(adminId)
                            .actionAt(rs.getObject("actionAt", LocalDateTime.class))
                            .reason(rs.getString("reason"))
                            .build();
                    return Optional.of(rl);
                }
            }
        }catch(SQLException e){
            LOG.log(Level.SEVERE,"Unable to find round lock",e);
            throw new DataAccessException("Unable to find round lock",e);
        }
        return Optional.empty();
    }

    @Override
    public List<RoundLock> getRoundLocksByRoundId(UUID roundId) {
        String query = "SELECT * FROM roundLock WHERE roundId = ?";
        List<RoundLock> roundLock = new ArrayList<>();
        try(Connection con = getConnection();
        PreparedStatement ps = con.prepareStatement(query)){
            ps.setString(1, roundId.toString());
            try(ResultSet rs = ps.executeQuery()){
                while(rs.next()){
                    String adminIdStr = rs.getString("action_by_admin_user_id");
                    UUID adminId = (adminIdStr != null) ? UUID.fromString(adminIdStr) : null;
                    RoundLock rl = RoundLock.builder()
                            .lockId(UUID.fromString(rs.getString("lockId")))
                            .roundId(UUID.fromString(rs.getString("roundId")))
                            .lockAction(RoundLockAction.valueOf(rs.getString("lockAction")))
                            .actionByAdminUserId(adminId)
                            .actionAt(rs.getObject("actionAt", LocalDateTime.class))
                            .reason(rs.getString("reason"))
                            .build();
                    roundLock.add(rl);
                }
            }
        }catch(SQLException e){
            LOG.log(Level.SEVERE,"Unable to find round lock",e);
            throw new DataAccessException("Unable to find round lock",e);
        }
        return roundLock;
    }

    @Override
    public Optional<RoundLock> getLatestRoundLockByRoundId(UUID roundId) {
        String query = "SELECT * FROM roundLock WHERE roundId = ? ORDER BY actionAt DESC LIMIT 1";
        try(Connection con = getConnection();
        PreparedStatement ps = con.prepareStatement(query)){
            ps.setString(1, roundId.toString());
            try(ResultSet rs = ps.executeQuery()){
                if(rs.next()){
                    String adminIdStr = rs.getString("action_by_admin_user_id");
                    UUID adminId = (adminIdStr != null) ? UUID.fromString(adminIdStr) : null;
                    RoundLock rl = RoundLock.builder()
                            .lockId(UUID.fromString(rs.getString("lockId")))
                            .roundId(UUID.fromString(rs.getString("roundId")))
                            .lockAction(RoundLockAction.valueOf(rs.getString("lockAction")))
                            .actionByAdminUserId(adminId)
                            .actionAt(rs.getObject("actionAt", LocalDateTime.class))
                            .reason(rs.getString("reason"))
                            .build();
                    return Optional.of(rl);
                }
            }
        }catch(SQLException e){
            LOG.log(Level.SEVERE,"Unable to find round lock",e);
            throw new DataAccessException("Unable to find round lock",e);
        }
        return Optional.empty();
    }

    @Override
    public List<RoundLock> getRoundLocksByAction(RoundLockAction lockAction) {
        String query = "SELECT * FROM roundLock WHERE lockAction = ?";
        List<RoundLock> roundLock = new ArrayList<>();
        try(Connection con = getConnection();
        PreparedStatement ps = con.prepareStatement(query)){
            ps.setString(1, lockAction.toString());
            try(ResultSet rs = ps.executeQuery()){
                while(rs.next()){
                    String adminIdStr = rs.getString("action_by_admin_user_id");
                    UUID adminId = (adminIdStr != null) ? UUID.fromString(adminIdStr) : null;
                    RoundLock rl = RoundLock.builder()
                            .lockId(UUID.fromString(rs.getString("lockId")))
                            .roundId(UUID.fromString(rs.getString("roundId")))
                            .lockAction(RoundLockAction.valueOf(rs.getString("lockAction")))
                            .actionByAdminUserId(adminId)
                            .actionAt(rs.getObject("actionAt", LocalDateTime.class))
                            .reason(rs.getString("reason"))
                            .build();
                            roundLock.add(rl);
                }
            }
        }catch(SQLException e){
            LOG.log(Level.SEVERE,"Unable to find round lock",e);
            throw new DataAccessException("Unable to find round lock",e);
        }
        return roundLock;
    }
}
