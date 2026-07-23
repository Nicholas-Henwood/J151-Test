package com.vzap.trytons.dao.results;

import com.vzap.trytons.exceptions.ConflictException;
import com.vzap.trytons.exceptions.DataAccessException;
import com.vzap.trytons.model.results.PlayerStatistics;
import jakarta.enterprise.context.ApplicationScoped;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import com.vzap.trytons.dao.shared.BaseDAO;

@ApplicationScoped
public class PlayerStatisticsDAOImpl extends BaseDAO implements PlayerStatisticsDAO {

    private static final Logger LOG = Logger.getLogger(PlayerStatisticsDAOImpl.class.getName());

    private static final String SELECT_COLUMNS = "SELECT statId, resultId, teamId, playerId, tries, assists, tackles, missedTackles, conversions, penalties, metersGained, yellowCards, redCards, statisticDate " +
            "FROM playerStatistics";

    private PlayerStatistics mapRow(ResultSet rs) throws SQLException {
        Timestamp statisticDate = rs.getTimestamp("statisticDate");
        return PlayerStatistics.builder()
                .statId(UUID.fromString(rs.getString("statId")))
                .resultId(UUID.fromString(rs.getString("resultId")))
                .teamId(UUID.fromString(rs.getString("teamId")))
                .playerId(UUID.fromString(rs.getString("playerId")))
                .tries(rs.getInt("tries"))
                .assists(rs.getInt("assists"))
                .tackles(rs.getInt("tackles"))
                .missedTackles(rs.getInt("missedTackles"))
                .conversions(rs.getInt("conversions"))
                .penalties(rs.getInt("penalties"))
                .metersGained(rs.getInt("metersGained"))
                .yellowCards(rs.getInt("yellowCards"))
                .redCards(rs.getInt("redCards"))
                .statisticDate(statisticDate == null ? null : statisticDate.toLocalDateTime())
                .build();
    }

    @Override
    public List<PlayerStatistics> findByResultId(UUID resultId) {
        return findMany(SELECT_COLUMNS + " WHERE resultId = ? ORDER BY teamId, playerId", resultId);
    }

    @Override
    public List<PlayerStatistics> findByResultIdAndTeamId(UUID resultId, UUID teamId) {
        List<PlayerStatistics> statistics = new ArrayList<>();
        String sql = SELECT_COLUMNS + " WHERE resultId = ? AND teamId = ? ORDER BY playerId";

        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, resultId.toString());
            statement.setString(2, teamId.toString());
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    statistics.add(mapRow(resultSet));
                }
            }
            return statistics;
        } catch (SQLException e) {
            LOG.log(Level.SEVERE, "Unable to retrieve player statistics for team.", e);
            throw new DataAccessException("Unable to retrieve player statistics for team.", e);
        }
    }

    @Override
    public Optional<PlayerStatistics> findByResultIdAndTeamIdAndPlayerId(
            UUID resultId,
            UUID teamId,
            UUID playerId) {

        String sql = SELECT_COLUMNS + " WHERE resultId = ? AND teamId = ? AND playerId = ?";
        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, resultId.toString());
            statement.setString(2, teamId.toString());
            statement.setString(3, playerId.toString());
            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next() ? Optional.of(mapRow(resultSet)) : Optional.empty();
            }
        } catch (SQLException e) {
            LOG.log(Level.SEVERE, "Unable to retrieve player statistic.", e);
            throw new DataAccessException("Unable to retrieve player statistic.", e);
        }
    }

    private List<PlayerStatistics> findMany(String sql, UUID resultId) {
        List<PlayerStatistics> statistics = new ArrayList<>();
        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, resultId.toString());
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    statistics.add(mapRow(resultSet));
                }
            }
            return statistics;
        } catch (SQLException e) {
            LOG.log(Level.SEVERE, "Unable to retrieve player statistics.", e);
            throw new DataAccessException("Unable to retrieve player statistics.", e);
        }
    }

    @Override
    public Optional<PlayerStatistics> save(PlayerStatistics statistics) {
        UUID statId = statistics.getStatId() == null ? UUID.randomUUID() : statistics.getStatId();
        LocalDateTime statisticDate = statistics.getStatisticDate() == null
                ? LocalDateTime.now()
                : statistics.getStatisticDate();

        String sql = "INSERT INTO playerStatistics (statId, resultId, teamId, playerId, tries, assists, tackles, missedTackles, conversions, penalties, metersGained, yellowCards, redCards, statisticDate)"+
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, statId.toString());
            statement.setString(2, statistics.getResultId().toString());
            statement.setString(3, statistics.getTeamId().toString());
            statement.setString(4, statistics.getPlayerId().toString());
            statement.setInt(5, statistics.getTries());
            statement.setInt(6, statistics.getAssists());
            statement.setInt(7, statistics.getTackles());
            statement.setInt(8, statistics.getMissedTackles());
            statement.setInt(9, statistics.getConversions());
            statement.setInt(10, statistics.getPenalties());
            statement.setInt(11, statistics.getMetersGained());
            statement.setInt(12, statistics.getYellowCards());
            statement.setInt(13, statistics.getRedCards());
            statement.setTimestamp(14, Timestamp.valueOf(statisticDate));

            if (statement.executeUpdate() != 1) {
                return Optional.empty();
            }
            return findByResultIdAndTeamIdAndPlayerId(
                    statistics.getResultId(),
                    statistics.getTeamId(),
                    statistics.getPlayerId()
            );
        } catch (SQLException e) {
            if ("45000".equals(e.getSQLState())) {
                throw new ConflictException(
                        e.getMessage() != null
                                ? e.getMessage()
                                : "The player statistics could not be saved because they conflict with an existing record."
                );
            }

            LOG.log(Level.SEVERE, "Unable to save player statistics.", e);
            throw new DataAccessException("Unable to save player statistics.", e);
        }
    }

    @Override
    public Optional<PlayerStatistics> findById(UUID statId) {
        String sql = SELECT_COLUMNS + " WHERE statId = ?";
        try (Connection connection = getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {

            ps.setString(1, statId.toString());

            try (ResultSet rs = ps.executeQuery()) {

                return rs.next() ? Optional.of(mapRow(rs)) : Optional.empty();
            }
        } catch (SQLException e) {
            LOG.log(Level.SEVERE, "Unable to retrieve player statistic by ID.", e);
            throw new DataAccessException("Unable to retrieve player statistic by ID.", e);
        }
    }
}
