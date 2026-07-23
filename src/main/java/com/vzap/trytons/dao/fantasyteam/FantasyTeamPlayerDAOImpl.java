package com.vzap.trytons.dao.fantasyteam;

import com.vzap.trytons.enums.SquadRole;
import com.vzap.trytons.exceptions.ConflictException;
import com.vzap.trytons.exceptions.DataAccessException;
import com.vzap.trytons.exceptions.ResourceNotFoundException;
import com.vzap.trytons.model.fantasyteam.PlayerSelectionCount;
import com.vzap.trytons.model.fantasyteam.TeamPlayerSelection;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import com.vzap.trytons.dao.shared.BaseDAO;

public class FantasyTeamPlayerDAOImpl extends BaseDAO implements FantasyTeamPlayerDAO {

    private static final Logger LOG = Logger.getLogger(FantasyTeamPlayerDAOImpl.class.getName());

    private TeamPlayerSelection mapSelection(ResultSet rs){
        try {
            TeamPlayerSelection selection = new TeamPlayerSelection();
            selection.setSelectionId(readUuid(rs, "selectionId"));
            selection.setTeamId(readUuid(rs, "teamId"));
            selection.setPlayerId(readUuid(rs, "playerId"));
            Timestamp selectedDate = rs.getTimestamp("selectedDate");
            selection.setSelectedDate(selectedDate != null ? selectedDate.toLocalDateTime() : null);
            selection.setIsCaptain(rs.getBoolean("isCaptain"));
            selection.setIsViceCaptain(rs.getBoolean("isViceCaptain"));
            selection.setSquadRole(SquadRole.valueOf(rs.getString("squadRole")));
            return selection;
        } catch(IllegalArgumentException e){
            LOG.log(Level.WARNING, "Error reading team player selection", e);
            throw new DataAccessException("Error reading team player selection", e);
        }catch(SQLException e){
            LOG.log(Level.SEVERE,"Cannot retrieve player selection",e);
            throw new DataAccessException("Cannot retrieve player selection",e);
        }
    }
    private static final String SELECTION_SELECT = "SELECT selectionId, teamId, playerId, selectedDate, isCaptain, " +
            "is_vice_captain AS isViceCaptain, squadRole FROM team_player_selection";

    private UUID readUuid(ResultSet rs, String name) throws SQLException {
        String value =  rs.getString(name);

        if (value == null){
            throw new SQLException("Database column " + name + " contains null value");
        }

        try{
            return UUID.fromString(value);
        }catch (IllegalArgumentException e){
            throw new SQLException("Database column " + name + " contains invalid UUID");
        }
    }

    @Override
    public boolean addPlayerToSquad(UUID teamId, UUID playerId, SquadRole squadRole) {
        String query = "INSERT INTO team_player_selection "
                + "(selectionId, teamId, playerId, selectedDate, isCaptain, is_vice_captain, squadRole) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?)";

        try(Connection con = getConnection();
            PreparedStatement ps = con.prepareStatement(query)) {
            ps.setString(1, UUID.randomUUID().toString());
            ps.setString(2, teamId.toString());
            ps.setString(3, playerId.toString());
            ps.setTimestamp(4, Timestamp.valueOf(LocalDateTime.now()));
            ps.setBoolean(5, false);
            ps.setBoolean(6, false);
            ps.setString(7, squadRole.name());

            return ps.executeUpdate() == 1;
        } catch (SQLException e) {
            String message = e.getMessage();

            if (message != null && message.contains("uk_team_player_selection")) {
                throw new ConflictException("Player is already in the squad");
            }

            LOG.log(Level.SEVERE, "Could not add player to squad", e);
            throw new DataAccessException("Could not add player to squad", e);
        }
    }

    @Override
    public void replaceSquad(UUID teamId, List<TeamPlayerSelection> squad) {
        String deleteQuery = "DELETE FROM team_player_selection WHERE teamId = ?";
        String insertQuery = "INSERT INTO team_player_selection "
                + "(selectionId, teamId, playerId, selectedDate, isCaptain, is_vice_captain, squadRole) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?)";

        Connection con = null; //Declared at the top instead of in try resource for rollback

        try {
            con = getConnection();
            con.setAutoCommit(false);

            try (
                    PreparedStatement dps = con.prepareStatement(deleteQuery);
                    PreparedStatement ips = con.prepareStatement(insertQuery)
            ) {
                dps.setString(1, teamId.toString());
                dps.executeUpdate();


                for (TeamPlayerSelection selection : squad) {
                    UUID selectionId = selection.getSelectionId() == null ? UUID.randomUUID() : selection.getSelectionId();
                    ips.setString(1, selectionId.toString());
                    ips.setString(2, teamId.toString());
                    ips.setString(3, selection.getPlayerId().toString());
                    LocalDateTime selectedDate = selection.getSelectedDate() == null ? LocalDateTime.now() : selection.getSelectedDate();
                    ips.setTimestamp(4, Timestamp.valueOf(selectedDate));
                    ips.setBoolean(5, Boolean.TRUE.equals(selection.getIsCaptain()));
                    ips.setBoolean(6, Boolean.TRUE.equals(selection.getIsViceCaptain()));
                    ips.setString(7,selection.getSquadRole().name());
                    ips.addBatch();
                }
                ips.executeBatch();
                con.commit();

            }

        } catch (SQLException e) {
            if (con != null) {
                try {
                    con.rollback();
                } catch (SQLException rollbackException) {
                    e.addSuppressed(rollbackException);
                }
            }

            LOG.log(Level.SEVERE, "Could not replace squad", e);
            throw new DataAccessException("Could not replace squad", e);

        } finally {
            if (con != null) {
                try {
                    con.setAutoCommit(true);
                    con.close();
                } catch (SQLException e) {
                    LOG.log(Level.SEVERE, "Could not close database connection", e);
                }
            }
        }
    }

    @Override
    public List<TeamPlayerSelection> getSquadByTeamId(UUID teamId) {
        String query = SELECTION_SELECT + " WHERE teamId = ? ORDER BY selectedDate ASC";
        List<TeamPlayerSelection> squad = new ArrayList<>();

        try(Connection con = getConnection();
            PreparedStatement ps = con.prepareStatement(query)) {
            ps.setString(1, teamId.toString());

            try(ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    squad.add(mapSelection(rs));
                }
            }
        } catch (SQLException e) {
            LOG.log(Level.SEVERE, "Could not get team by its ID", e);
            throw new DataAccessException("Could not get team by its ID", e);
        }
        return squad;
    }

    @Override
    public Optional<TeamPlayerSelection> findSquadEntry(UUID teamId, UUID playerId) {
        String query = SELECTION_SELECT + " WHERE teamId = ? AND playerId = ?";

        try(Connection con = getConnection();
        PreparedStatement ps = con.prepareStatement(query)){
            ps.setString(1, teamId.toString());
            ps.setString(2, playerId.toString());

            try(ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapSelection(rs));
                }
            }
        } catch (SQLException e) {
            LOG.log(Level.SEVERE, "Could not get squad entry", e);
            throw new DataAccessException("Could not get squad entry", e);
        }
        return Optional.empty();
    }

    @Override
    public boolean removePlayerFromSquad(UUID teamId, UUID playerId) {
        String query =  "DELETE FROM team_player_selection WHERE teamId = ? AND playerId = ?";

        try(Connection con = getConnection();
            PreparedStatement ps = con.prepareStatement(query)){
            ps.setString(1, teamId.toString());
            ps.setString(2, playerId.toString());

            return ps.executeUpdate() >= 1;
        } catch (SQLException e) {
            LOG.log(Level.SEVERE, "Could not remove player from squad", e);
            throw new DataAccessException("Could not remove player from squad", e);
        }
    }

    @Override
    public boolean updateSquadRole(UUID teamId, UUID playerId, SquadRole squadRole) {
        String query = "UPDATE team_player_selection SET squadRole = ? WHERE teamId = ? AND playerId = ?";
        try(Connection con = getConnection();
        PreparedStatement ps = con.prepareStatement(query)){
            ps.setString(1, squadRole.name());
            ps.setString(2, teamId.toString());
            ps.setString(3, playerId.toString());
            return ps.executeUpdate() >= 1;
        }catch(SQLException e){
            LOG.log(Level.SEVERE, "Could not update squad role", e);
            throw new DataAccessException("Could not update squad role", e);
        }
    }

    @Override
    public boolean setCaptain(UUID teamId, UUID playerId) {
        String oldCaptain = " UPDATE team_player_selection SET isCaptain = FALSE  WHERE teamId = ? AND isCaptain = TRUE";
        String newCaptain = " UPDATE team_player_selection SET isCaptain = TRUE  WHERE teamId = ? AND playerId = ?";
        Connection con = null;
        try{
            con = getConnection();
            con.setAutoCommit(false);
            try(PreparedStatement oldPs = con.prepareStatement(oldCaptain);
                PreparedStatement newPs = con.prepareStatement(newCaptain)
            ){
                oldPs.setString(1, teamId.toString());
                oldPs.executeUpdate();

                newPs.setString(1,teamId.toString());
                newPs.setString(2,playerId.toString());

                int update = newPs.executeUpdate();
                if(update == 0){
                    con.rollback();
                    throw new ResourceNotFoundException("Player not found in this team's squad");
                }
                con.commit();
                return true;
            }
        }catch(SQLException e){
            if(con != null) {
                try {
                    con.rollback();
                } catch (SQLException rollbackException) {
                    e.addSuppressed(rollbackException);
                }
            }
            LOG.log(Level.SEVERE, "Could not update captain", e);
            throw new DataAccessException("Could not update captain", e);
        }finally{
            if(con != null) {
                try {
                    con.setAutoCommit(true);
                    con.close();
                } catch (SQLException e) {
                    LOG.log(Level.SEVERE, "Could not close database connection", e);
                }
            }
        }
    }

    @Override
    public boolean setViceCaptain(UUID teamId, UUID playerId) {
        String oldViceCaptain = "UPDATE team_player_selection SET is_vice_captain = FALSE  WHERE teamId = ? AND is_vice_captain = TRUE";
        String newViceCaptain = "UPDATE team_player_selection SET is_vice_captain = TRUE  WHERE teamId = ? AND playerId = ?";
        Connection con = null;
        try{
            con = getConnection();
            con.setAutoCommit(false);
            try(PreparedStatement oldPs = con.prepareStatement(oldViceCaptain);
                PreparedStatement newPs = con.prepareStatement(newViceCaptain)
            ){
                oldPs.setString(1, teamId.toString());
                oldPs.executeUpdate();

                newPs.setString(1,teamId.toString());
                newPs.setString(2,playerId.toString());

                int update = newPs.executeUpdate();
                if(update == 0){
                    con.rollback();
                    throw new ResourceNotFoundException("Player not found in this team's squad");
                }
                con.commit();
                return true;
            }
        }catch(SQLException e){
            if(con != null) {
                try {
                    con.rollback();
                } catch (SQLException rollbackException) {
                    e.addSuppressed(rollbackException);
                }
            }
            LOG.log(Level.SEVERE, "Could not update  vice captain", e);
            throw new DataAccessException("Could not update  vice captain", e);
        }finally{
            if(con != null) {
                try {
                    con.setAutoCommit(true);
                    con.close();
                } catch (SQLException e) {
                    LOG.log(Level.SEVERE, "Could not close database connection", e);
                }
            }
        }
    }

    @Override
    public boolean clearCaptain(UUID teamId) {
        String query = " UPDATE team_player_selection SET isCaptain = FALSE WHERE teamId = ? AND isCaptain = TRUE";
        try(Connection con = getConnection();
        PreparedStatement ps = con.prepareStatement(query)){
            ps.setString(1, teamId.toString());
            return ps.executeUpdate() >= 1;
        }catch(SQLException e){
            LOG.log(Level.SEVERE, "Could not clear captain", e);
            throw new DataAccessException("Could not clear captain", e);
        }
    }

    @Override
    public boolean clearViceCaptain(UUID teamId) {
        String query = "UPDATE team_player_selection SET is_vice_captain = FALSE WHERE teamId = ? AND is_vice_captain = TRUE";
        try(Connection con = getConnection();
            PreparedStatement ps = con.prepareStatement(query)){
            ps.setString(1, teamId.toString());
            return ps.executeUpdate() >= 1;
        }catch(SQLException e){
            LOG.log(Level.SEVERE, "Could not clear vice captain", e);
            throw new DataAccessException("Could not clear vice captain", e);
        }
    }

    @Override
    public List<PlayerSelectionCount> findMostSelectedPlayers(int limit) {
        String query = """
            SELECT playerId, COUNT(*) AS selectionCount
            FROM team_player_selection
            GROUP BY playerId
            ORDER BY selectionCount DESC
            LIMIT ?""";
        try(Connection con = getConnection();
        PreparedStatement ps = con.prepareStatement(query)){
            ps.setInt(1, limit);
            try(ResultSet rs = ps.executeQuery()){
                List<PlayerSelectionCount> playerSelectionCounts = new ArrayList<>();
                while(rs.next()){
                playerSelectionCounts.add(new PlayerSelectionCount(UUID.fromString(rs.getString("playerId")), rs.getInt("selectionCount")));
                }
                return playerSelectionCounts;
            }
        }catch(SQLException e){
            LOG.log(Level.SEVERE, "Could not find most selected players", e);
            throw new DataAccessException("Could not find most selected players", e);
        }
    }


    @Override
    public List<UUID> getTeamIdsByPlayerId(UUID playerId) {
        String query = "SELECT DISTINCT teamId FROM team_player_selection WHERE playerId = ?";
        List<UUID> teamIds = new ArrayList<>();

        try(Connection con = getConnection();
            PreparedStatement ps = con.prepareStatement(query)) {
            ps.setString(1, playerId.toString());

            try(ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    teamIds.add(UUID.fromString(rs.getString("teamId")));
                }
            }
        } catch (SQLException e) {
            LOG.log(Level.SEVERE, "Could not find teams containing player", e);
            throw new DataAccessException("Could not find teams containing player", e);
        }
        return teamIds;
    }
}
