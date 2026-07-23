package com.vzap.trytons.dao.catalog;

import com.vzap.trytons.enums.AvailabilityStatus;
import com.vzap.trytons.exceptions.DataAccessException;
import com.vzap.trytons.model.catalog.Player;
import com.vzap.trytons.model.catalog.PlayerAvailability;
import jakarta.inject.Singleton;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import com.vzap.trytons.dao.shared.BaseDAO;

@Singleton
public class PlayerDAOImpl extends BaseDAO implements PlayerDAO {

    private static final Logger LOG = Logger.getLogger(PlayerDAOImpl.class.getName());
    private static final String PLAYER_SELECT = "SELECT * FROM player ";

    private Player mapPlayer(ResultSet rs) {
        try {
            return Player.builder()
                    .playerId(UUID.fromString(rs.getString("playerId")))
                    .clubId(UUID.fromString(rs.getString("clubId")))
                    .positionId(UUID.fromString(rs.getString("positionId")))
                    .playerName(rs.getString("playerName"))
                    .value(rs.getBigDecimal("value"))
                    .attackingAbility(rs.getInt("attackingAbility"))
                    .defensiveAbility(rs.getInt("defensiveAbility"))
                    .kickingAbility(rs.getInt("kickingAbility"))
                    .discipline(rs.getInt("discipline"))
                    .consistency(rs.getInt("consistency"))
                    .fitness(rs.getInt("fitness"))
                    .currentForm(rs.getInt("currentForm"))
                    .isActive(rs.getBoolean("isActive"))
                    .build();

        } catch (SQLException | IllegalArgumentException e) {
            LOG.log(Level.SEVERE, "Unable to map player result.", e);
            throw new DataAccessException("Unable to map player result.", e);
        }
    }

    private PlayerAvailability mapAvailability(ResultSet rs) {
        try {
            String statusValue = rs.getString("status");

            if (statusValue == null) {
                throw new DataAccessException("Availability status cannot be null.", null);
            }

            AvailabilityStatus status;

            try {
                status = AvailabilityStatus.valueOf(statusValue);
            } catch (IllegalArgumentException e) {
                throw new DataAccessException("Invalid availability status stored in database.", e);
            }

            Date effectiveDate = rs.getDate("effectiveDate");
            Date endDate = rs.getDate("endDate");

            PlayerAvailability availability = new PlayerAvailability();
            availability.setAvailabilityId(UUID.fromString(rs.getString("availabilityId")));
            availability.setPlayerId(UUID.fromString(rs.getString("playerId")));
            availability.setStatus(status);
            availability.setEffectiveDate(effectiveDate != null ? effectiveDate.toLocalDate() : null);
            availability.setEndDate(endDate != null ? endDate.toLocalDate() : null);
            availability.setNotes(rs.getString("notes"));

            return availability;

        } catch (SQLException | IllegalArgumentException e) {
            LOG.log(Level.SEVERE, "Unable to map player availability result.", e);
            throw new DataAccessException("Unable to map player availability result.", e);
        }
    }

    @Override
    public Optional<Player> getPlayerById(UUID playerId) {
        String query = PLAYER_SELECT + "WHERE playerId = ?";

        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement(query)) {

            ps.setString(1, playerId.toString());
            try (ResultSet rs = ps.executeQuery()) {

                if (rs.next()) {
                    return Optional.of(mapPlayer(rs));
                }
            }

        } catch (SQLException e) {
            LOG.log(Level.SEVERE, "Unable to retrieve player by ID.", e);
            throw new DataAccessException("Unable to retrieve player by ID.", e);
        }

        return Optional.empty();
    }

    @Override
    public List<Player> getAllPlayers() {
        String query = PLAYER_SELECT + "ORDER BY playerName ASC";
        List<Player> players = new ArrayList<>();

        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement(query);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                players.add(mapPlayer(rs));
            }

        } catch (SQLException e) {
            LOG.log(Level.SEVERE, "Unable to retrieve all players.", e);
            throw new DataAccessException("Unable to retrieve all players.", e);
        }
        return players;
    }

    @Override
    public List<Player> searchPlayers(String playerName, UUID clubId, UUID positionId, BigDecimal minValue, BigDecimal maxValue, Integer minCurrentForm, Integer maxCurrentForm, AvailabilityStatus availabilityStatus, Boolean isActive) {

        String query = PLAYER_SELECT + "WHERE 1 = 1 ";

        if (playerName != null && !playerName.isBlank()) {
            query = query + "AND LOWER(playerName) LIKE ? ";
        }

        if (clubId != null) {
            query = query + "AND clubId = ? ";
        }

        if (positionId != null) {
            query = query + "AND positionId = ? ";
        }

        if (minValue != null) {
            query = query + "AND value >= ? ";
        }

        if (maxValue != null) {
            query = query + "AND value <= ? ";
        }

        if (minCurrentForm != null) {
            query = query + "AND currentForm >= ? ";
        }

        if (maxCurrentForm != null) {
            query = query + "AND currentForm <= ? ";
        }

        if (availabilityStatus != null) {
            query = query
                    + "AND EXISTS (SELECT 1 FROM playerAvailability "
                    + "WHERE playerAvailability.playerId = player.playerId "
                    + "AND playerAvailability.status = ? "
                    + "AND playerAvailability.effectiveDate <= CURRENT_DATE "
                    + "AND (playerAvailability.endDate IS NULL "
                    + "OR playerAvailability.endDate >= CURRENT_DATE)) ";
        }

        if (isActive != null) {
            query = query + "AND isActive = ? ";
        }

        query = query + "ORDER BY playerName ASC";

        List<Player> players = new ArrayList<>();

        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement(query)) {

            int parameterIndex = 1;

            if (playerName != null && !playerName.isBlank()) {
                ps.setString(parameterIndex++, "%" + playerName.trim().toLowerCase(Locale.ROOT) + "%");
            }

            if (clubId != null) {
                ps.setString(parameterIndex++, clubId.toString());
            }

            if (positionId != null) {
                ps.setString(parameterIndex++, positionId.toString());
            }

            if (minValue != null) {
                ps.setBigDecimal(parameterIndex++, minValue);
            }

            if (maxValue != null) {
                ps.setBigDecimal(parameterIndex++, maxValue);
            }

            if (minCurrentForm != null) {
                ps.setInt(parameterIndex++, minCurrentForm);
            }

            if (maxCurrentForm != null) {
                ps.setInt(parameterIndex++, maxCurrentForm);
            }

            if (availabilityStatus != null) {
                ps.setString(parameterIndex++, availabilityStatus.name());
            }

            if (isActive != null) {
                ps.setBoolean(parameterIndex, isActive);
            }

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    players.add(mapPlayer(rs));
                }
            }

        } catch (SQLException e) {
            LOG.log(Level.SEVERE, "Unable to search players.", e);
            throw new DataAccessException("Unable to search players.", e);
        }

        return players;
    }

    @Override
    public Optional<Player> createPlayer(Player player) {
        UUID playerId = player.getPlayerId() != null ? player.getPlayerId() : UUID.randomUUID();

        String query = "INSERT INTO player (playerId, clubId, positionId, playerName, value, attackingAbility, defensiveAbility, kickingAbility, discipline, consistency, fitness, currentForm) "
                        + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement(query)) {

            ps.setString(1, playerId.toString());
            ps.setString(2, player.getClubId().toString());
            ps.setString(3, player.getPositionId().toString());
            ps.setString(4, player.getPlayerName());
            ps.setBigDecimal(5, player.getValue());
            ps.setInt(6, player.getAttackingAbility());
            ps.setInt(7, player.getDefensiveAbility());
            ps.setInt(8, player.getKickingAbility());
            ps.setInt(9, player.getDiscipline());
            ps.setInt(10, player.getConsistency());
            ps.setInt(11, player.getFitness());
            ps.setInt(12, player.getCurrentForm());

            if (ps.executeUpdate() == 1) {
                return getPlayerById(playerId);
            }

        } catch (SQLException e) {
            LOG.log(Level.SEVERE, "Unable to create player.", e);
            throw new DataAccessException("Unable to create player.", e);
        }

        return Optional.empty();
    }

    @Override
    public Optional<Player> updatePlayer(Player player) {
        String query = "UPDATE player SET clubId = ?, positionId = ?, playerName = ?, value = ?, attackingAbility = ?, defensiveAbility = ?, "
                        + "kickingAbility = ?, discipline = ?, consistency = ?, fitness = ?, currentForm = ? WHERE playerId = ?";

        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement(query)) {

            ps.setString(1, player.getClubId().toString());
            ps.setString(2, player.getPositionId().toString());
            ps.setString(3, player.getPlayerName());
            ps.setBigDecimal(4, player.getValue());
            ps.setInt(5, player.getAttackingAbility());
            ps.setInt(6, player.getDefensiveAbility());
            ps.setInt(7, player.getKickingAbility());
            ps.setInt(8, player.getDiscipline());
            ps.setInt(9, player.getConsistency());
            ps.setInt(10, player.getFitness());
            ps.setInt(11, player.getCurrentForm());
            ps.setString(12, player.getPlayerId().toString());

            if (ps.executeUpdate() == 1) {
                return getPlayerById(player.getPlayerId());
            }

        } catch (SQLException e) {
            LOG.log(Level.SEVERE, "Unable to update player.", e);
            throw new DataAccessException("Unable to update player.", e);
        }

        return Optional.empty();
    }

    @Override
    public boolean deactivatePlayer(UUID playerId) {
        String query = "UPDATE player SET isActive = FALSE WHERE playerId = ? AND isActive = TRUE";

        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement(query)) {

            ps.setString(1, playerId.toString());
            return ps.executeUpdate() == 1;

        } catch (SQLException e) {
            LOG.log(Level.SEVERE, "Unable to deactivate player.", e);
            throw new DataAccessException("Unable to deactivate player.", e);
        }
    }

    @Override
    public List<Player> getPlayersByClubId(UUID clubId) {
        String query = PLAYER_SELECT + "WHERE clubId = ? ORDER BY playerName ASC";
        List<Player> players = new ArrayList<>();

        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement(query)) {

            ps.setString(1, clubId.toString());

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    players.add(mapPlayer(rs));
                }
            }

        } catch (SQLException e) {
            LOG.log(Level.SEVERE, "Unable to retrieve players for the club.", e);
            throw new DataAccessException("Unable to retrieve players for the club.", e);
        }

        return players;
    }

    @Override
    public List<Player> getPlayersByPositionId(UUID positionId) {
        String query = PLAYER_SELECT + "WHERE positionId = ? ORDER BY playerName ASC";
        List<Player> players = new ArrayList<>();

        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement(query)) {

            ps.setString(1, positionId.toString());

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    players.add(mapPlayer(rs));
                }
            }

        } catch (SQLException e) {
            LOG.log(Level.SEVERE, "Unable to retrieve players for the position.", e);
            throw new DataAccessException("Unable to retrieve players for the position.", e);
        }

        return players;
    }

    @Override
    public Optional<PlayerAvailability> getCurrentAvailability(UUID playerId) {
        String query = "SELECT * FROM playerAvailability WHERE playerId = ? "
                        + "AND effectiveDate <= CURRENT_DATE "
                        + "AND (endDate IS NULL OR endDate >= CURRENT_DATE) "
                        + "ORDER BY effectiveDate DESC, availabilityId DESC LIMIT 1";

        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement(query)) {

            ps.setString(1, playerId.toString());

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapAvailability(rs));
                }
            }

        } catch (SQLException e) {
            LOG.log(Level.SEVERE, "Unable to retrieve current player availability.", e);
            throw new DataAccessException("Unable to retrieve current player availability.", e);
        }

        return Optional.empty();
    }

    @Override
    public List<PlayerAvailability> getAvailabilityHistory(UUID playerId) {
        String query = "SELECT * FROM playerAvailability WHERE playerId = ? "
                        + "ORDER BY effectiveDate DESC, availabilityId DESC";

        List<PlayerAvailability> availabilityHistory = new ArrayList<>();

        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement(query)) {

            ps.setString(1, playerId.toString());

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    availabilityHistory.add(mapAvailability(rs));
                }
            }

        } catch (SQLException e) {
            LOG.log(Level.SEVERE, "Unable to retrieve player availability history.", e);
            throw new DataAccessException("Unable to retrieve player availability history.", e);
        }

        return availabilityHistory;
    }
}