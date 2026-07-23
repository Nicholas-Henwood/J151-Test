package com.vzap.trytons.dao.results;

import com.vzap.trytons.enums.MatchTeamSide;
import com.vzap.trytons.exceptions.ConflictException;
import com.vzap.trytons.exceptions.DataAccessException;
import com.vzap.trytons.model.results.MatchTeamScore;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import com.vzap.trytons.dao.shared.BaseDAO;

public class MatchTeamScoreDAOImpl extends BaseDAO implements MatchTeamScoreDAO {

    private static final Logger LOG = Logger.getLogger(MatchTeamScoreDAOImpl.class.getName());

    private MatchTeamScore mapRow(ResultSet rs) throws SQLException {
        return MatchTeamScore.builder()
                .scoreId(UUID.fromString(rs.getString("scoreId")))
                .resultId(UUID.fromString(rs.getString("resultId")))
                .teamId(UUID.fromString(rs.getString("teamId")))
                .teamSide(MatchTeamSide.valueOf(rs.getString("teamSide")))
                .playerPoints(rs.getInt("playerPoints"))
                .captainBonus(rs.getInt("captainBonus"))
                .transferPenalty(rs.getInt("transferPenalty"))
                .totalScore(rs.getInt("totalScore"))
                .calculatedAt(rs.getObject("calculatedAt", LocalDateTime.class))
                .build();
    }


    @Override
    public MatchTeamScore save(MatchTeamScore matchTeamScore) {
        UUID newScoreId = UUID.randomUUID();

        String query = "INSERT INTO match_team_score "
                + "(scoreId, resultId, teamId, teamSide, playerPoints, captainBonus, transferPenalty, calculatedAt) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement(query)) {

            ps.setString(1, newScoreId.toString());
            ps.setString(2, matchTeamScore.getResultId().toString());
            ps.setString(3, matchTeamScore.getTeamId().toString());
            ps.setString(4, matchTeamScore.getTeamSide().name());
            ps.setInt(5, matchTeamScore.getPlayerPoints());
            ps.setInt(6, matchTeamScore.getCaptainBonus());
            ps.setInt(7, matchTeamScore.getTransferPenalty());
            ps.setTimestamp(8, Timestamp.valueOf(matchTeamScore.getCalculatedAt()));

            ps.executeUpdate();

            return findById(newScoreId)
                    .orElseThrow(() -> new DataAccessException("Saved match team score could not be re-read", null));

        } catch (SQLException e) {
            if ("45000".equals(e.getSQLState())) {
                throw new ConflictException(
                        e.getMessage() != null
                                ? e.getMessage()
                                : "The match team score could not be saved because it conflicts with an existing record."
                );
            }

            LOG.log(Level.SEVERE, "Unable to save match team score", e);
            throw new DataAccessException("Unable to save match team score", e);
        }
    }

    @Override
    public MatchTeamScore update(MatchTeamScore matchTeamScore) {
        String query = "UPDATE match_team_score SET "
                + "playerPoints = ?, captainBonus = ?, transferPenalty = ?, calculatedAt = ? "
                + "WHERE scoreId = ?";

        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement(query)) {

            ps.setInt(1, matchTeamScore.getPlayerPoints());
            ps.setInt(2, matchTeamScore.getCaptainBonus());
            ps.setInt(3, matchTeamScore.getTransferPenalty());
            ps.setTimestamp(4, Timestamp.valueOf(matchTeamScore.getCalculatedAt()));
            ps.setString(5, matchTeamScore.getScoreId().toString());

            ps.executeUpdate();

            return findById(matchTeamScore.getScoreId())
                    .orElseThrow(() -> new DataAccessException("Updated match team score could not be re-read", null));

        } catch (SQLException e) {
            if ("45000".equals(e.getSQLState())) {
                throw new ConflictException(
                        e.getMessage() != null
                                ? e.getMessage()
                                : "The match team score could not be updated because it conflicts with an existing record."
                );
            }

            LOG.log(Level.SEVERE, "Unable to update match team score", e);
            throw new DataAccessException("Unable to update match team score", e);
        }
    }

    @Override
    public Optional<MatchTeamScore> findById(UUID scoreId) {
        String query =
                "SELECT scoreId, resultId, teamId, teamSide, playerPoints, captainBonus, transferPenalty, totalScore, calculatedAt " +
                        "FROM match_team_score " +
                        "WHERE scoreId = ?";

        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement(query)) {

            ps.setString(1, scoreId.toString());

            try (ResultSet rs = ps.executeQuery()) {

                if (rs.next()) {
                    MatchTeamScore currentScore = mapRow(rs);
                    return Optional.of(currentScore);
                }
            }

        } catch (SQLException e) {
            LOG.log(Level.SEVERE, "Unable to find match team score by ID", e);
            throw new DataAccessException("Unable to find match team score by ID", e);
        }

        return Optional.empty();
    }

    @Override
    public List<MatchTeamScore> findByResultId(UUID resultId) {
        String query =
                "SELECT scoreId, resultId, teamId, teamSide, playerPoints, captainBonus, transferPenalty, totalScore, calculatedAt " +
                        "FROM match_team_score " +
                        "WHERE resultId = ?";

        List<MatchTeamScore> matchTeamScores = new ArrayList<>();

        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement(query)) {

            ps.setString(1, resultId.toString());

            try (ResultSet rs = ps.executeQuery()) {

                while (rs.next()) {
                    MatchTeamScore currentScore = mapRow(rs);
                    matchTeamScores.add(currentScore);
                }
            }

        } catch (SQLException e) {
            LOG.log(Level.SEVERE, "Unable to find match team score by result ID", e);
            throw new DataAccessException("Unable to find match team score by result ID", e);
        }

        return matchTeamScores;
    }

    @Override
    public Optional<MatchTeamScore> findByResultIdAndTeamSide(UUID resultId, MatchTeamSide teamSide) {
        String query =
                "SELECT scoreId, resultId, teamId, teamSide, playerPoints, captainBonus, transferPenalty, totalScore, calculatedAt " +
                        "FROM match_team_score " +
                        "WHERE resultId = ? AND teamSide = ?";

        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement(query)) {

            ps.setString(1, resultId.toString());
            ps.setString(2, teamSide.name());

            try (ResultSet rs = ps.executeQuery()) {

                if (rs.next()) {
                    MatchTeamScore currentScore = mapRow(rs);
                    return Optional.of(currentScore);
                }
            }

        } catch (SQLException e) {
            LOG.log(Level.SEVERE, "Unable to find match team score by result ID and team side", e);
            throw new DataAccessException("Unable to find match team score by result ID and team side", e);
        }

        return Optional.empty();
    }
}