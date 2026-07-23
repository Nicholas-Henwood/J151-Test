package com.vzap.trytons.dao.fantasyteam;

import com.vzap.trytons.exceptions.ConflictException;
import com.vzap.trytons.exceptions.DataAccessException;
import com.vzap.trytons.model.fantasyteam.FantasyTeam;
import jakarta.enterprise.context.ApplicationScoped;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import com.vzap.trytons.dao.shared.BaseDAO;

@ApplicationScoped
public class FantasyTeamDAOImpl extends BaseDAO implements FantasyTeamDAO {

    private static final Logger LOG = Logger.getLogger(FantasyTeamDAOImpl.class.getName());

    private FantasyTeam mapTeam(ResultSet rs){
        try {
            FantasyTeam team = new FantasyTeam();
            team.setTeamId(UUID.fromString(rs.getString("teamId")));
            team.setTeamName(rs.getString("teamName"));
            team.setRemainingBudget(rs.getBigDecimal("remainingBudget"));
            Timestamp creationDate = rs.getTimestamp("creationDate");
            if(creationDate != null){
                team.setCreationDate(creationDate.toLocalDateTime());
            }
            team.setIsValid(rs.getBoolean("isValid"));
            team.setOwnerUserId(UUID.fromString(rs.getString("owner_user_id")));
            return team;
        }catch(SQLException e) {
            LOG.log(Level.SEVERE, " Fantasy team cannot be retrieved", e);
            throw new DataAccessException("Fantasy team cannot be retrieved", e);
        }
    }

    @Override
    public Optional<FantasyTeam> createTeam(FantasyTeam team) {
        UUID teamId = team.getTeamId() == null ? UUID.randomUUID() : team.getTeamId();
        LocalDateTime creationDate = team.getCreationDate() == null ? LocalDateTime.now() : team.getCreationDate();
        String sql = "INSERT INTO fantasyTeam (teamId, owner_user_id, teamName, remainingBudget, creationDate, isValid)"+
                "VALUES (?, ?, ?, ?, ?, ?)";

        try (Connection connection = getConnection();PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, teamId.toString());
            statement.setString(2, team.getOwnerUserId().toString());
            statement.setString(3, team.getTeamName());
            statement.setBigDecimal(4, team.getRemainingBudget() == null ? BigDecimal.ZERO : team.getRemainingBudget());
            statement.setTimestamp(5, Timestamp.valueOf(creationDate));
            statement.setBoolean(6, Boolean.TRUE.equals(team.getIsValid()));

            if (statement.executeUpdate() == 1) {
                return getTeamById(teamId);
            }
            return Optional.empty();
        } catch (SQLException e) {
            String message = e.getMessage();
            if (message != null && message.contains("uk_fantasyTeam_owner")) {
                throw new ConflictException("This user already owns a fantasy team.");
            }
            if (message != null && message.contains("uk_fantasyTeam_teamName")) {
                throw new ConflictException("This team name is already taken.");
            }
            LOG.log(Level.SEVERE, "Unable to create fantasy team.", e);
            throw new DataAccessException("Unable to create fantasy team.", e);
        }
    }

    @Override
    public Optional<FantasyTeam> getTeamById(UUID teamId) {
        String sql = "SELECT * FROM fantasyTeam WHERE teamId = ?";
        try (Connection connection = getConnection(); PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, teamId.toString());
            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next() ? Optional.of(mapTeam(resultSet)) : Optional.empty();
            }
        } catch (SQLException e) {
            LOG.log(Level.SEVERE, "Unable to retrieve fantasy team.", e);
            throw new DataAccessException("Unable to retrieve fantasy team.", e);
        }
    }

    @Override
    public FantasyTeam findTeamById(UUID teamId) {
        return getTeamById(teamId).orElse(null);
    }

    @Override
    public Optional<FantasyTeam> getTeamByOwner(UUID owner_user_id) {
        String sql = "SELECT * FROM fantasyTeam WHERE owner_user_id = ?";
        try (Connection connection = getConnection(); PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, owner_user_id.toString());
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return Optional.of(mapTeam(resultSet));
                }
                return Optional.empty();
            }
        } catch (SQLException e) {
            LOG.log(Level.SEVERE, "Unable to retrieve fantasy team by owner.", e);
            throw new DataAccessException("Unable to retrieve fantasy team by owner.", e);
        }
    }

    @Override
    public boolean updateTeamName(UUID teamId, String teamName) {
        String query = "UPDATE fantasyTeam SET teamName = ? WHERE teamId = ?";
        try(Connection conn = getConnection();
        PreparedStatement ps = conn.prepareStatement(query)){
            ps.setString(1, teamName);
            ps.setString(2, teamId.toString());
            return ps.executeUpdate() == 1;
        }catch(SQLException e) {
            String message = e.getMessage();
            if(message != null && message.contains("uk_fantasyTeam_teamName")) {
                throw new ConflictException("This team name is already taken.");
            }
            LOG.log(Level.SEVERE, "Unable to update fantasy team name.", e);
            throw new DataAccessException("Unable to update fantasy team name.", e);
        }

    }

    @Override
    public boolean updateBudget(UUID teamId, BigDecimal remainingBudget) {
        String sql = "UPDATE fantasyTeam SET remainingBudget = ? WHERE teamId = ?";
        try (Connection connection = getConnection(); PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setBigDecimal(1, remainingBudget);
            statement.setString(2, teamId.toString());
            return statement.executeUpdate() == 1;
        } catch (SQLException e) {
            LOG.log(Level.SEVERE, "Unable to update fantasy team budget.", e);
            throw new DataAccessException("Unable to update fantasy team budget.", e);
        }
    }
}
