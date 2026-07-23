package com.vzap.trytons.dao.results;

import com.vzap.trytons.enums.MatchTeamSide;
import com.vzap.trytons.exceptions.ConflictException;
import com.vzap.trytons.exceptions.DataAccessException;
import com.vzap.trytons.model.results.MatchResult;
import jakarta.inject.Singleton;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import com.vzap.trytons.dao.shared.BaseDAO;

@Singleton
public class MatchResultDAOImpl extends BaseDAO implements MatchResultDAO {

    private static final Logger LOG = Logger.getLogger(MatchResultDAOImpl.class.getName());

    private static final String MATCH_RESULT_SELECT = """
            SELECT
                mr.resultId AS resultId,
                mr.fixtureId AS fixtureId,
                mr.settingsId AS settingsId,
                mr.team_a_score AS teamAScore,
                mr.team_b_score AS teamBScore,
                mr.winnerSide AS winnerSide,
                mr.isDraw AS isDraw,
                mr.resultDate AS resultDate,
                mr.approved AS approved,
                mr.approved_by_admin_user_id AS approvedByAdminId,
                mr.simulation_run_number AS simulationRunNumber,
                mr.isCurrent AS isCurrent
            FROM matchResult mr
            """;

    private MatchResult mapMatchResult(ResultSet resultSet) throws SQLException {

        Timestamp resultDate = resultSet.getTimestamp("resultDate");

        String approvedByAdminId = resultSet.getString("approvedByAdminId");

        String winnerSideStr = resultSet.getString("winnerSide");

        return MatchResult.builder()
                .resultId(UUID.fromString(resultSet.getString("resultId")))
                .fixtureId(UUID.fromString(resultSet.getString("fixtureId")))
                .settingsId(UUID.fromString(resultSet.getString("settingsId")))
                .simulationRunNumber(resultSet.getInt("simulationRunNumber"))
                .teamAScore(resultSet.getInt("teamAScore"))
                .teamBScore(resultSet.getInt("teamBScore"))
                .winnerSide(winnerSideStr == null ? null : MatchTeamSide.valueOf(winnerSideStr))
                .isDraw(resultSet.getBoolean("isDraw"))
                .approved(resultSet.getBoolean("approved"))
                .isCurrent(resultSet.getBoolean("isCurrent"))
                .resultDate(resultDate == null ? null : resultDate.toLocalDateTime())
                .approvedByAdminUserId(approvedByAdminId == null ? null: UUID.fromString(approvedByAdminId))
                .build();
    }

    @Override
    public MatchResult save(MatchResult matchResult) {

        if (matchResult == null) {
            throw new DataAccessException("Match result cannot be null.", null);
        }

        if (matchResult.getFixtureId() == null) {
            throw new DataAccessException("Fixture ID is required when saving a match result.", null);
        }

        if (matchResult.getSettingsId() == null) {
            throw new DataAccessException("Settings ID is required when saving a match result.", null);
        }

        UUID resultId = matchResult.getResultId() == null ? UUID.randomUUID() : matchResult.getResultId();

        String query =
                "INSERT INTO matchResult"+
                    "(resultId, fixtureId, settingsId, team_a_score, team_b_score, winnerSide, isDraw, approved, isCurrent, resultDate, approved_by_admin_user_id, simulation_run_number)"+
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";


        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {

            statement.setString(1, resultId.toString());
            statement.setString(2, matchResult.getFixtureId().toString());
            statement.setString(3, matchResult.getSettingsId().toString());
            statement.setInt(4, matchResult.getTeamAScore());
            statement.setInt(5, matchResult.getTeamBScore());
            if (matchResult.getWinnerSide() == null){
                statement.setNull(6, Types.VARCHAR);
            }else{
                statement.setString(6, matchResult.getWinnerSide().name());
            }
            statement.setBoolean(7, matchResult.isDraw());
            statement.setBoolean(8, matchResult.isApproved());
            statement.setBoolean(9, matchResult.isCurrent());
            if (matchResult.getResultDate() == null) {
                statement.setNull(10, Types.TIMESTAMP);
            } else {
                statement.setTimestamp(10, Timestamp.valueOf(matchResult.getResultDate()));
            }
            if (matchResult.getApprovedByAdminUserId() == null) {
                statement.setNull(11, Types.VARCHAR);
            } else {
                statement.setString(11, matchResult.getApprovedByAdminUserId().toString());
            }
            statement.setInt(12, matchResult.getSimulationRunNumber());

            int affectedRows = statement.executeUpdate();

            if (affectedRows != 1) {
                throw new DataAccessException("Unable to save match result.", null);
            }

            matchResult.setResultId(resultId);

            return findById(resultId).orElseThrow(() -> new DataAccessException("The match result was saved but could not be retrieved.", null));

        } catch (SQLException e) {
            if ("45000".equals(e.getSQLState())) {
                throw new ConflictException(
                        e.getMessage() != null
                                ? e.getMessage()
                                : "The match result could not be saved because it conflicts with an existing record."
                );
            }

            LOG.log(Level.SEVERE, "Unable to save match result.", e);
            throw new DataAccessException("Unable to save match result.", e);
        }
    }

    @Override
    public Optional<MatchResult> findById(UUID resultId) {

        if (resultId == null) {
            return Optional.empty();
        }

        String query =
                MATCH_RESULT_SELECT
                        + " WHERE mr.resultId = ?";

        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {

            statement.setString(
                    1,
                    resultId.toString()
            );

            try (ResultSet resultSet = statement.executeQuery()) {

                if (resultSet.next()) {
                    return Optional.of(mapMatchResult(resultSet));
                }
            }

        } catch (SQLException e) {
            LOG.log(Level.SEVERE, "Unable to find match result by ID.", e);
            throw new DataAccessException("Unable to find match result by ID.", e);
        }

        return Optional.empty();
    }

    @Override
    public Optional<MatchResult> findCurrentByFixtureId(
            UUID fixtureId) {

        if (fixtureId == null) {
            return Optional.empty();
        }

        String query = MATCH_RESULT_SELECT + "WHERE mr.fixtureId = ?"+
                "AND mr.isCurrent = TRUE"+
                "ORDER BY mr.simulation_run_number DESC"+
                "LIMIT 1";

        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {

            statement.setString(1, fixtureId.toString());

            try (ResultSet resultSet = statement.executeQuery()) {

                if (resultSet.next()) {
                    return Optional.of(mapMatchResult(resultSet));
                }
            }

        } catch (SQLException e) {
            LOG.log(
                    Level.SEVERE, "Unable to retrieve the current match result.", e
            );

            throw new DataAccessException("Unable to retrieve the current match result.", e);
        }

        return Optional.empty();
    }

    @Override
    public List<MatchResult> findAllByFixtureId(
            UUID fixtureId) {

        List<MatchResult> results = new ArrayList<>();

        if (fixtureId == null) {
            return results;
        }

        String query = MATCH_RESULT_SELECT + "WHERE mr.fixtureId = ?"+
                "ORDER BY mr.simulation_run_number ASC";

        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {

            statement.setString(1, fixtureId.toString());

            try (ResultSet resultSet = statement.executeQuery()) {

                while (resultSet.next()) {
                    results.add(mapMatchResult(resultSet));
                }
            }

            return results;

        } catch (SQLException e) {

            LOG.log(Level.SEVERE, "Unable to retrieve match results for the fixture.", e);
            throw new DataAccessException("Unable to retrieve match results for the fixture.", e);
        }
    }

    @Override
    public int getNextSimulationRunNumber(
            UUID fixtureId) {

        if (fixtureId == null) {
            throw new DataAccessException("Fixture ID is required.", null);
        }

        String query =
                "SELECT MAX(simulation_run_number)"+
                "FROM matchResult"+
                "WHERE fixtureId = ?";


        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {

            statement.setString(1, fixtureId.toString());

            try (ResultSet resultSet = statement.executeQuery()) {

                if (resultSet.next()) {
                    int currentRunNumber = resultSet.getInt(1);

                    if (resultSet.wasNull()) {
                        return 1;
                    }
                    return currentRunNumber + 1;
                }
            }

            return 1;

        } catch (SQLException e) {
            LOG.log(Level.SEVERE, "Unable to determine the next simulation run number.", e);

            throw new DataAccessException("Unable to determine the next simulation run number.", e);
        }
    }

    @Override
    public int markAllFixtureResultsNotCurrent(
            UUID fixtureId) {

        if (fixtureId == null) {
            throw new DataAccessException("Fixture ID is required.", null);
        }

        String query = "UPDATE matchResult"+
                "SET isCurrent = FALSE"+
                "WHERE fixtureId = ?"+
                "AND isCurrent = TRUE";


        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {

            statement.setString(1, fixtureId.toString());

            return statement.executeUpdate();

        } catch (SQLException e) {
            if ("45000".equals(e.getSQLState())) {
                throw new ConflictException(e.getMessage() != null ? e.getMessage() : "The previous match result could not be superseded because its stored data is inconsistent.");
            }
            LOG.log(Level.SEVERE, "Unable to mark previous fixture results as not current.", e);
            throw new DataAccessException("Unable to mark previous fixture results as not current.", e);
        }
    }
}