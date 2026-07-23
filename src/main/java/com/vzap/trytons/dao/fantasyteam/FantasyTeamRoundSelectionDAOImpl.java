package com.vzap.trytons.dao.fantasyteam;

import com.vzap.trytons.enums.SquadRole;
import com.vzap.trytons.exceptions.ConflictException;
import com.vzap.trytons.exceptions.DataAccessException;
import com.vzap.trytons.model.fantasyteam.FantasyTeamRoundSelection;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import com.vzap.trytons.dao.shared.BaseDAO;

public class FantasyTeamRoundSelectionDAOImpl extends BaseDAO implements FantasyTeamRoundSelectionDAO {
    private static final Logger LOGGER = Logger.getLogger(FantasyTeamRoundSelectionDAOImpl.class.getName());
    
    private FantasyTeamRoundSelection mapRoundSelection (ResultSet rs) throws SQLException {
        FantasyTeamRoundSelection selection = new FantasyTeamRoundSelection();
        selection.setSelectionId(UUID.fromString(rs.getString("selectionId")));
        selection.setRoundId(UUID.fromString(rs.getString("roundId")));
        selection.setTeamId(UUID.fromString(rs.getString("teamId")));
        selection.setPlayerId(UUID.fromString(rs.getString("playerId")));
        selection.setSelectedDate(rs.getTimestamp("selectedDate").toLocalDateTime());
        selection.setSquadRole(SquadRole.valueOf(rs.getString("squadRole")));
        selection.setIsCaptain(rs.getBoolean("isCaptain"));
        selection.setIsViceCaptain(rs.getBoolean("is_vice_captain"));
        selection.setLockedAt(rs.getTimestamp("lockedAt").toLocalDateTime());
        return selection;
    }
    
    @Override
    public Optional<FantasyTeamRoundSelection> createRoundSelection(FantasyTeamRoundSelection selection) {
        String query = "INSERT " +
                "INTO fantasy_team_round_selection(selectionId, roundId, teamId, playerId, selectedDate, squadRole, isCaptain, is_vice_captain, lockedAt) " +
                "VALUES (?,?,?,?,?,?,?,?,?)";
        try(Connection con = getConnection();
            PreparedStatement ps = con.prepareStatement(query)) {
            UUID newSelectionId = UUID.randomUUID();
            ps.setString(1, newSelectionId.toString());
            ps.setString(2,selection.getRoundId().toString());
            ps.setString(3,selection.getTeamId().toString());
            ps.setString(4,selection.getPlayerId().toString());
            if(selection.getSelectedDate() != null) {
                ps.setTimestamp(5, Timestamp.valueOf(selection.getSelectedDate()));
            }else{
                ps.setTimestamp(5, Timestamp.valueOf(LocalDateTime.now()));
            }
            ps.setString(6, selection.getSquadRole().name());
            if(selection.getIsCaptain() != null) {
                ps.setBoolean(7,selection.getIsCaptain());
            }else{
                ps.setBoolean(7,false);
            }
            if(selection.getIsViceCaptain() != null) {
                ps.setBoolean(8,selection.getIsViceCaptain());
            }else{
                ps.setBoolean(8,false);
            }
            if(selection.getLockedAt() != null) {
                ps.setTimestamp(9, Timestamp.valueOf(selection.getLockedAt()));
            }else {
                ps.setTimestamp(9, Timestamp.valueOf(LocalDateTime.now()));
            }
            ps.executeUpdate();
            selection.setSelectionId(newSelectionId);
            return Optional.of(selection);
        }catch(SQLException e){
            if ("45000".equals(e.getSQLState())) {
                throw new ConflictException(e.getMessage() != null ? e.getMessage() : "The round selection could not be created because it conflicts with an existing record.");
            }

            LOGGER.log(Level.SEVERE,"Unable to create new fantasy team round selection",e);
            throw new DataAccessException("Unable to create new fantasy team round selection",e);
        }
    }

    @Override
    public int createRoundSelections(List<FantasyTeamRoundSelection> selections) {
        String query = "INSERT INTO fantasy_team_round_selection(selectionId, roundId, teamId, playerId, selectedDate, squadRole, isCaptain, is_vice_captain, lockedAt) VALUES (?,?,?,?,?,?,?,?,?)";
        Connection con = null;
        PreparedStatement ps = null;
        try {
            con = getConnection();
            con.setAutoCommit(false);
            ps = con.prepareStatement(query);
            for (FantasyTeamRoundSelection selection : selections) {
                UUID newSelectionId = UUID.randomUUID();
                selection.setSelectionId(newSelectionId);
                ps.setString(1, newSelectionId.toString());
                ps.setString(2, selection.getRoundId().toString());
                ps.setString(3, selection.getTeamId().toString());
                ps.setString(4, selection.getPlayerId().toString());
                if (selection.getSelectedDate() != null) {
                    ps.setTimestamp(5, Timestamp.valueOf(selection.getSelectedDate()));
                } else {
                    ps.setTimestamp(5, Timestamp.valueOf(LocalDateTime.now()));
                }
                ps.setString(6, selection.getSquadRole().name());
                if (selection.getIsCaptain() != null) {
                    ps.setBoolean(7, selection.getIsCaptain());
                } else {
                    ps.setBoolean(7, false);
                }
                if (selection.getIsViceCaptain() != null) {
                    ps.setBoolean(8, selection.getIsViceCaptain());
                } else {
                    ps.setBoolean(8, false);
                }
                if (selection.getLockedAt() != null) {
                    ps.setTimestamp(9, Timestamp.valueOf(selection.getLockedAt()));
                } else {
                    ps.setTimestamp(9, Timestamp.valueOf(LocalDateTime.now()));
                }
                ps.addBatch();
            }
            int[] result = ps.executeBatch();
            con.commit();
            int rowsInserted = 0;
            for(int count : result) {
                    rowsInserted += count;
            }
            return rowsInserted;
        } catch (SQLException e) {
            if (con != null) {
                try {
                    con.rollback();
                } catch (SQLException ex) {
                    LOGGER.log(Level.SEVERE, "Unable to rollback transaction", ex);
                }
            }

            if ("45000".equals(e.getSQLState())) {
                throw new ConflictException(e.getMessage() != null ? e.getMessage() : "The round selections could not be created because they conflict with an existing record.");
            }

            LOGGER.log(Level.SEVERE, "Unable to create new fantasy team round selections", e);
            throw new DataAccessException("Unable to create new fantasy team round selections", e);
        } finally {
            if (ps != null) {
                try {
                    ps.close();
                }catch (SQLException e){
                    LOGGER.log(Level.SEVERE, "Unable to close statement", e);
                }
            }
            if (con != null) {
                try {
                    con.setAutoCommit(true);
                    con.close();
                } catch (SQLException e) {
                    LOGGER.log(Level.SEVERE, "Unable to close connection", e);
                }
            }
        }
    }

    @Override
    public Optional<FantasyTeamRoundSelection> getRoundSelectionById(UUID selectionId) {
        String query = "SELECT * FROM fantasy_team_round_selection WHERE selectionId = ?";
        try(Connection con = getConnection();
        PreparedStatement ps = con.prepareStatement(query)){
            ps.setString(1, selectionId.toString());
            try (ResultSet rs = ps.executeQuery()){
                if(rs.next()){
                    return Optional.of(mapRoundSelection(rs));
                }
                return Optional.empty();
            }
        }catch(SQLException e){
            LOGGER.log(Level.SEVERE,"Unable to get fantasy team round selection",e);
            throw new DataAccessException("Unable to get fantasy team round selection",e);
        }

    }

    @Override
    public List<FantasyTeamRoundSelection> getSelectionsByRoundId(UUID roundId) {
        List<FantasyTeamRoundSelection> selections = new ArrayList<>();
        String query = "SELECT * FROM fantasy_team_round_selection WHERE roundId = ?";
        try(Connection con = getConnection();
        PreparedStatement ps = con.prepareStatement(query)){
            ps.setString(1, roundId.toString());
            try(ResultSet rs = ps.executeQuery()){
                while(rs.next()){
                    selections.add(mapRoundSelection(rs));
                }
                return selections;
            }
        }catch(SQLException e){
            LOGGER.log(Level.SEVERE,"Unable to get fantasy team round selection",e);
            throw new DataAccessException("Unable to get fantasy team round selection",e);
        }
    }

    @Override
    public List<FantasyTeamRoundSelection> getSelectionsByRoundIdAndTeamId(UUID roundId, UUID teamId) {
        List<FantasyTeamRoundSelection> selections = new ArrayList<>();
        String query = "SELECT * FROM fantasy_team_round_selection WHERE roundId = ? AND teamId = ?";
        try(Connection con = getConnection();
        PreparedStatement ps = con.prepareStatement(query)){
            ps.setString(1, roundId.toString());
            ps.setString(2, teamId.toString());
            try(ResultSet rs = ps.executeQuery()){
                while(rs.next()){
                    selections.add(mapRoundSelection(rs));
                }
                return selections;
            }
        }catch(SQLException e){
            LOGGER.log(Level.SEVERE,"Unable to get fantasy team round selection",e);
            throw new DataAccessException("Unable to get fantasy team round selection",e);
        }
    }

    @Override
    public List<UUID> getTeamIdsWithSnapshotsForRound(UUID roundId) {
        List<UUID> teamIds = new ArrayList<>();
        String query = "SELECT DISTINCT teamId FROM fantasy_team_round_selection WHERE roundId = ?";
        try(Connection con = getConnection();
        PreparedStatement ps = con.prepareStatement(query)){
            ps.setString(1, roundId.toString());
            try(ResultSet rs = ps.executeQuery()){
                while(rs.next()){
                    teamIds.add(UUID.fromString(rs.getString("teamId")));
                }
                return teamIds;
            }
        }catch(SQLException e){
            LOGGER.log(Level.SEVERE,"Unable to retrieve team ID's",e);
            throw new DataAccessException("Unable to retrieve team ID's",e);
        }
    }

    @Override
    public boolean snapshotsExistForRound(UUID roundId) {
        return countSelectionsByRoundId(roundId) > 0;
    }

    @Override
    public boolean snapshotExistsForTeamInRound(UUID roundId, UUID teamId) {
        return countSelectionsByRoundIdAndTeamId(roundId, teamId) > 0;
    }

    @Override
    public int countSelectionsByRoundId(UUID roundId) {
        String query = "SELECT COUNT(*) FROM fantasy_team_round_selection WHERE roundId = ?";
        try(Connection con = getConnection();
        PreparedStatement ps = con.prepareStatement(query)){
            ps.setString(1, roundId.toString());
            try(ResultSet rs = ps.executeQuery()){
                if(rs.next()){
                    return rs.getInt(1);
                }
                return 0;
            }
        }catch(SQLException e){
            LOGGER.log(Level.SEVERE,"Unable to count selections for round",e);
            throw new DataAccessException("Unable to count selections for round",e);
        }
    }

    @Override
    public int countSelectionsByRoundIdAndTeamId(UUID roundId, UUID teamId) {
        String query = "SELECT COUNT(*) FROM fantasy_team_round_selection WHERE roundId = ? AND teamId = ?";
        try(Connection con = getConnection();
        PreparedStatement ps = con.prepareStatement(query)){
            ps.setString(1, roundId.toString());
            ps.setString(2, teamId.toString());
            try(ResultSet rs = ps.executeQuery()) {
                if(rs.next()){
                    return rs.getInt(1);
                }
                return 0;
            }
        }catch(SQLException e){
            LOGGER.log(Level.SEVERE,"Unable to count selections",e);
            throw new DataAccessException("Unable to count selections",e);
        }
    }
}
