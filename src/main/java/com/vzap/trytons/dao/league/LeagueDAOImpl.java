package com.vzap.trytons.dao.league;
import com.vzap.trytons.enums.LeagueType;
import com.vzap.trytons.exceptions.DataAccessException;
import com.vzap.trytons.model.league.League;
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

public class LeagueDAOImpl extends BaseDAO implements LeagueDAO {
   private static final Logger LOG = Logger.getLogger((LeagueDAOImpl.class.getName()));

    @Override
    public League createLeague(League league) {
        String query = "INSERT INTO league(leagueId,manager_user_id,leagueName,description,leagueType,leagueCode,isActive,maxMembers) VALUES (?,?,?,?,?,?,?,?)";
        try(Connection con = getConnection();
        PreparedStatement ps = con.prepareStatement(query);){
            ps.setString(1,league.getLeagueId().toString());
            ps.setString(2,toNullableId(league.getManagerUserId()));
            ps.setString(3,league.getLeagueName());
            ps.setString(4,league.getDescription());
            ps.setString(5,league.getLeagueType().toString());
            ps.setString(6,league.getLeagueCode());
            ps.setBoolean(7,league.getIsActive() == null || league.getIsActive());
            ps.setInt(8,league.getMaxMembers());
            if (ps.executeUpdate() > 0){
                return league;
            }
        }catch(SQLException e){
            LOG.log(Level.SEVERE, "Unable to create League", e);
            throw new DataAccessException("Unable to create League", e);
        }
        return null;
    }

    @Override
    public Optional<League> findLeagueById(UUID leagueId) {
        String query = "SELECT * FROM league WHERE leagueId = ?";
        try(Connection con = getConnection();
        PreparedStatement ps = con.prepareStatement(query);){
            ps.setString(1, leagueId.toString());
            try(ResultSet rs = ps.executeQuery();){
                if(rs.next()){
                    return Optional.of(this.rowToLeague(rs));
                }
            }
        }catch(SQLException e){
            LOG.log(Level.SEVERE, "Unable to find League", e);
            throw new DataAccessException("Unable to find League", e);
        }
        return Optional.empty();
    }

    @Override
    public Optional<League> findLeagueByName(String leagueName) {
        String query = "SELECT * FROM league WHERE leagueName = ?";
        try(Connection con = getConnection();
        PreparedStatement ps = con.prepareStatement(query);){
            ps.setString(1, leagueName);
            try(ResultSet rs = ps.executeQuery();){
                if(rs.next()){
                    return Optional.of(this.rowToLeague(rs));
                }
            }
        }catch(SQLException e){
            LOG.log(Level.SEVERE, "Unable to find League by name", e);
            throw new DataAccessException("Unable to find League by name", e);
        }
        return Optional.empty();
    }

    @Override
    public List<League> findAllLeagues() {
        List<League> leagues = new ArrayList<>();
        String query = "SELECT * FROM league";
        try(Connection con = getConnection();
        PreparedStatement ps = con.prepareStatement(query);
        ResultSet rs = ps.executeQuery();){
            while(rs.next()){
                leagues.add(this.rowToLeague(rs));
            }
        }catch(SQLException e){
            LOG.log(Level.SEVERE, "Unable to find Leagues", e);
            throw new DataAccessException("Unable to find Leagues", e);
        }
        return leagues;
    }

    @Override
    public List<League> findLeaguesByLeagueManager(UUID manager_user_id) {
        List<League> leagues = new ArrayList<>();
        String query = "SELECT * FROM league WHERE manager_user_id = ?";
        try(Connection con = getConnection();
        PreparedStatement ps = con.prepareStatement(query);){
            ps.setString(1,manager_user_id.toString());
            try(ResultSet rs = ps.executeQuery();){
                while(rs.next()){
                    leagues.add(this.rowToLeague(rs));
                }
            }
        }catch(SQLException e){
            LOG.log(Level.SEVERE, "Unable to find League by manager ID", e);
            throw new DataAccessException("Unable to find League by manager ID", e);
        }
        return leagues;
    }

    @Override
    public List<League> findLeaguesByManagerName(String username) {
        List<League> leagues = new ArrayList<>();
        String query = "SELECT l.* FROM league l INNER JOIN registeredUser ru ON l.manager_user_id = ru.userId INNER JOIN user u ON ru.userId = u.userId WHERE u.username = ?";
        try(Connection con = getConnection();
        PreparedStatement ps = con.prepareStatement(query);){
            ps.setString(1,username);
            try(ResultSet rs = ps.executeQuery();){
                while(rs.next()){
                    leagues.add(this.rowToLeague(rs));
                }
            }
        }catch(SQLException e){
            LOG.log(Level.SEVERE, "Unable to find League by manager name", e);
            throw new DataAccessException("Unable to find League by manager name", e);
        }
        return leagues;
    }

    @Override
    public Optional<League> findLeagueByLeagueCode(String leagueCode) {
        String query = "SELECT * FROM league WHERE leagueCode = ?";
        try(Connection con = getConnection();
        PreparedStatement ps = con.prepareStatement(query);){
            ps.setString(1, leagueCode);
            try(ResultSet rs = ps.executeQuery();){
                if(rs.next()){
                    return Optional.of(this.rowToLeague(rs));
                }
            }
        }catch(SQLException e){
            LOG.log(Level.SEVERE, "Unable to find League by league code", e);
            throw new DataAccessException("Unable to find League by league code", e);
        }
        return Optional.empty();
    }

    @Override
    public boolean existsByLeagueCode(String leagueCode) {
        String query = "SELECT COUNT(*) FROM league WHERE leagueCode = ?";
        try(Connection con = getConnection();
        PreparedStatement ps = con.prepareStatement(query);){
            ps.setString(1,leagueCode);
            try(ResultSet rs = ps.executeQuery();){
                if(rs.next()){
                    return rs.getInt(1) > 0;
                }
                return false;
            }
        }catch(SQLException e){
            LOG.log(Level.SEVERE, "Unable to find League by league code", e);
            throw new DataAccessException("Unable to find League by league code", e);
        }
    }

    @Override
    public boolean deactivateLeague(UUID leagueId) {
        String query = "UPDATE league SET isActive = FALSE WHERE leagueId = ?";
        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement(query)) {
            ps.setString(1, leagueId.toString());
            return ps.executeUpdate() > 0;
        }catch(SQLException e){
            LOG.log(Level.SEVERE,"League cannot be deactivated",e);
            throw new DataAccessException("League cannot be deactivated", e);
        }
    }

    @Override
    public boolean updateLeague(League league) {
        String query = "UPDATE league SET leagueName = ?, description = ?, isActive = ?, maxMembers = ? WHERE leagueId = ?";
        try(Connection con = getConnection();
        PreparedStatement ps = con.prepareStatement(query)) {
            ps.setString(1,league.getLeagueName());
            ps.setString(2,league.getDescription());
            ps.setBoolean(3,league.getIsActive() == null || league.getIsActive());
            ps.setInt(4,league.getMaxMembers());
            ps.setString(5,league.getLeagueId().toString());
            return ps.executeUpdate() > 0;
        }catch(SQLException e){
            LOG.log(Level.SEVERE,"League cannot be updated",e);
            throw new DataAccessException("League cannot be updated", e);
        }
    }

    @Override
    public boolean deleteLeague(UUID leagueId) {
        String query = "DELETE FROM league WHERE leagueId = ?";
        try(Connection con = getConnection();
        PreparedStatement pr = con.prepareStatement(query);){
            pr.setString(1,leagueId.toString());
            return pr.executeUpdate() > 0;
        }catch(SQLException e){
            LOG.log(Level.SEVERE,"League cannot be deleted",e);
            throw new DataAccessException("League cannot be deleted", e);
        }
    }

    private static String toNullableId(UUID id) {
        return id == null ? null : id.toString();
    }

    private League rowToLeague(ResultSet rs) throws SQLException {
        League league = new League();
        league.setLeagueId(UUID.fromString(rs.getString("leagueId")));
        league.setLeagueName(rs.getString("leagueName"));
        league.setDescription(rs.getString("description"));
        league.setLeagueType(LeagueType.valueOf(rs.getString("leagueType")));
        league.setLeagueCode(rs.getString("leagueCode"));
        league.setCreationDate(rs.getTimestamp("creationDate").toLocalDateTime());
        league.setIsActive(rs.getBoolean("isActive"));
        league.setMaxMembers(rs.getInt("maxMembers"));
        String manager_user_id = rs.getString("manager_user_id");
        if(manager_user_id!=null){
            league.setManagerUserId(UUID.fromString(manager_user_id));
        }
        return league;
    }

    @Override
    public boolean assignManager(UUID leagueId, UUID managerUserId) {
        String query = "UPDATE league SET manager_user_id = ? WHERE leagueId = ?";
        try(Connection con = getConnection();
            PreparedStatement ps = con.prepareStatement(query)) {
            ps.setString(1, managerUserId.toString());
            ps.setString(2, leagueId.toString());
            return ps.executeUpdate() > 0;
        }catch(SQLException e){
            LOG.log(Level.SEVERE,"Unable to assign manager for league " + leagueId, e);
            throw new DataAccessException("Unable to assign manager for league " + leagueId, e);
        }
    }
}