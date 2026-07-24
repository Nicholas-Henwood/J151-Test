package com.vzap.trytons.dao.scoring;

import com.vzap.trytons.exceptions.ConflictException;
import com.vzap.trytons.exceptions.DataAccessException;
import com.vzap.trytons.model.scoring.ScoringRule;
import jakarta.ejb.Singleton;

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

@Singleton
public class ScoringRuleDAOImpl extends BaseDAO implements ScoringRuleDAO {

    private static final Logger LOG = Logger.getLogger(ScoringRuleDAOImpl.class.getName());

    @Override
    public List<ScoringRule> findActiveRules(String season) {

        List<ScoringRule> rules = new ArrayList<>();

        String query = "SELECT ruleId, season, eventType, pointsAwarded, " +
                        "isDeduction, description, isActive " +
                        "FROM scoringRule " +
                        "WHERE season = ? AND isActive = TRUE " +
                        "ORDER BY eventType";

        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement(query)) {

            ps.setString(1, season);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    rules.add(mapRow(rs));
                }
            }

        } catch (SQLException e) {
            LOG.log(Level.SEVERE, "Unable to find active scoring rules", e);
            throw new DataAccessException("Unable to find active scoring rules", e);
        }

        return rules;
    }

    @Override
    public Optional<ScoringRule> findById(UUID ruleId) {

        String query =
                "SELECT ruleId, season, eventType, pointsAwarded, " +
                        "isDeduction, description, isActive " +
                        "FROM scoringRule " +
                        "WHERE ruleId = ?";

        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement(query)) {

            ps.setString(1, ruleId.toString());

            try (ResultSet rs = ps.executeQuery()) {

                if (rs.next()) {
                    return Optional.of(mapRow(rs));
                }
            }

        } catch (SQLException e) {
            LOG.log(Level.SEVERE, "Unable to find scoring rule by ID", e);
            throw new DataAccessException("Unable to find scoring rule by ID", e);
        }

        return Optional.empty();
    }

    @Override
    public Optional<ScoringRule> findBySeasonAndEventType(String season, String eventType) {

        String query = "SELECT ruleId, season, eventType, pointsAwarded, " +
                        "isDeduction, description, isActive " +
                        "FROM scoringRule " +
                        "WHERE season = ? AND eventType = ?";

        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement(query)) {

            ps.setString(1, season);
            ps.setString(2, eventType);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRow(rs));
                }
            }

        } catch (SQLException e) {
            LOG.log(Level.SEVERE, "Unable to find scoring rule by season and event type", e);
            throw new DataAccessException("Unable to find scoring rule by season and event type", e);
        }

        return Optional.empty();
    }

    @Override
    public ScoringRule save(ScoringRule rule) {

        UUID newRuleId = UUID.randomUUID();
        String query = "INSERT INTO scoringRule (ruleId, season, eventType, pointsAwarded, isDeduction, description, isActive) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?)";

        boolean deduction = Boolean.TRUE.equals(rule.getIsDeduction());

        boolean active = Boolean.TRUE.equals(rule.getIsActive());

        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement(query)) {

            ps.setString(1, newRuleId.toString());
            ps.setString(2, rule.getSeason());
            ps.setString(3, rule.getEventType());
            ps.setInt(4, rule.getPointsAwarded());
            ps.setBoolean(5, deduction);
            ps.setString(6, rule.getDescription());
            ps.setBoolean(7, active);

            int rowsAffected = ps.executeUpdate();

            if (rowsAffected == 0) {
                throw new SQLException("No scoring rule was saved");
            }

            rule.setRuleId(newRuleId);
            rule.setIsDeduction(deduction);
            rule.setIsActive(active);

            return rule;

        } catch (SQLException e) {
            if ("45000".equals(e.getSQLState())) {
                throw new ConflictException(e.getMessage() != null ? e.getMessage() : "The scoring rule could not be saved because it conflicts with an existing season ruleset.");
            }

            String message = e.getMessage();
            if (message != null && message.contains("uk_scoringRule_season_event")) {
                throw new ConflictException("A scoring rule already exists for this season and event type.");
            }

            LOG.log(Level.SEVERE, "Unable to save scoring rule", e);
            throw new DataAccessException("Unable to save scoring rule", e);
        }
    }

    @Override
    public ScoringRule update(ScoringRule rule) {

        String query = "UPDATE scoringRule " +
                "SET season = ?, eventType = ?, pointsAwarded = ?, isDeduction = ?, description = ?, isActive = ? "+
                "WHERE ruleId = ?";

        boolean deduction =
                Boolean.TRUE.equals(rule.getIsDeduction());

        boolean active =
                Boolean.TRUE.equals(rule.getIsActive());

        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement(query)) {

            ps.setString(1, rule.getSeason());
            ps.setString(2, rule.getEventType());
            ps.setInt(3, rule.getPointsAwarded());
            ps.setBoolean(4, deduction);
            ps.setString(5, rule.getDescription());
            ps.setBoolean(6, active);
            ps.setString(7, rule.getRuleId().toString());

            int rowsAffected = ps.executeUpdate();

            if (rowsAffected == 0) {
                throw new SQLException("No scoring rule was updated");
            }

            rule.setIsDeduction(deduction);
            rule.setIsActive(active);

            return rule;

        } catch (SQLException e) {
            if ("45000".equals(e.getSQLState())) {
                throw new ConflictException(
                        e.getMessage() != null
                                ? e.getMessage()
                                : "The scoring rule could not be updated because it conflicts with an existing season ruleset."
                );
            }

            LOG.log(Level.SEVERE, "Unable to update scoring rule", e);
            throw new DataAccessException("Unable to update scoring rule", e);
        }
    }

    @Override
    public boolean seasonHasResults(String season) {

        // Mirrors the trg_scoringRule_season_locked_* triggers so the UI can tell,
        // ahead of any write, whether the season's ruleset is locked.
        String query = "SELECT 1 " +
                "FROM matchResult mr " +
                "JOIN fixture f ON f.fixtureId = mr.fixtureId " +
                "JOIN fantasyRound fr ON fr.roundId = f.roundId " +
                "WHERE fr.season = ? " +
                "LIMIT 1";

        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement(query)) {

            ps.setString(1, season);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }

        } catch (SQLException e) {
            LOG.log(Level.SEVERE, "Unable to determine whether season has results", e);
            throw new DataAccessException("Unable to determine whether season has results", e);
        }
    }

    private ScoringRule mapRow(ResultSet rs) throws SQLException {

        ScoringRule rule = new ScoringRule();
        rule.setRuleId(UUID.fromString(rs.getString("ruleId")));
        rule.setSeason(rs.getString("season"));
        rule.setEventType(rs.getString("eventType"));
        rule.setPointsAwarded(rs.getInt("pointsAwarded"));
        rule.setIsDeduction(rs.getBoolean("isDeduction"));
        rule.setDescription(rs.getString("description"));
        rule.setIsActive(rs.getBoolean("isActive"));

        return rule;
    }
}