package com.vzap.trytons.dao.scoring;

import com.vzap.trytons.exceptions.ConflictException;
import com.vzap.trytons.exceptions.DataAccessException;
import com.vzap.trytons.model.scoring.FantasyPointBreakdown;

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

public class FantasyPointBreakdownDAOImpl extends BaseDAO implements FantasyPointBreakdownDAO {
    private static final Logger LOG = Logger.getLogger(FantasyPointBreakdownDAOImpl.class.getName());

    @Override
    public FantasyPointBreakdown save(FantasyPointBreakdown fantasyPointBreakdown) {

        UUID newId = UUID.randomUUID();

        String query = "INSERT INTO fantasy_point_breakdown"
                + "(breakdownId, pointsId, ruleId, eventCount, pointsEarned) "
                + "VALUES (?, ?, ?, ?, ?)";

        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement(query)) {

            ps.setString(1, newId.toString());
            ps.setString(2, fantasyPointBreakdown.getPointsId().toString());
            ps.setString(3, fantasyPointBreakdown.getRuleId().toString());
            ps.setInt(4, fantasyPointBreakdown.getEventCount());
            ps.setInt(5, fantasyPointBreakdown.getPointsEarned());

            ps.executeUpdate();

            fantasyPointBreakdown.setBreakdownId(newId);

            return fantasyPointBreakdown;

        } catch (SQLException e) {
            if ("45000".equals(e.getSQLState())) {
                throw new ConflictException(e.getMessage() != null ? e.getMessage() : "The fantasy point breakdown could not be saved because it conflicts with an existing record.");
            }

            String message = e.getMessage();
            if (message != null && message.contains("uk_fantasy_point_breakdown_rule")) {
                throw new ConflictException("A fantasy point breakdown already exists for this points and rule combination.");
            }

            LOG.log(Level.SEVERE, "Could not save fantasy points breakdown", e);
            throw new DataAccessException("Could not save fantasy points breakdown", e);
        }

    }

    @Override
    public Optional<FantasyPointBreakdown> findById(UUID breakdownId) {

        String query = "SELECT * FROM fantasy_point_breakdown WHERE breakdownId = ?";

        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement(query)) {

            ps.setString(1, breakdownId.toString());

            try(ResultSet rs = ps.executeQuery()){
                if(rs.next()){

                    FantasyPointBreakdown fpb = FantasyPointBreakdown.builder()
                            .breakdownId(breakdownId)
                            .pointsId(UUID.fromString(rs.getString("pointsId")))
                            .ruleId(UUID.fromString(rs.getString("ruleId")))
                            .eventCount(rs.getInt("eventCount"))
                            .pointsEarned(rs.getInt("pointsEarned"))
                            .build();

                    return Optional.of(fpb);

                }
            }
        } catch (SQLException e) {
            LOG.log(Level.SEVERE, "Could not find fantasy points breakdown by ID", e);
            throw new DataAccessException("Could not find fantasy points breakdown by ID", e);
        }

        return Optional.empty();

    }

    @Override
    public List<FantasyPointBreakdown> findByPointsId(UUID pointsId) {

        String query = "SELECT * FROM fantasy_point_breakdown WHERE pointsId = ?";

        List<FantasyPointBreakdown> breakdowns = new ArrayList<>();

        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement(query)) {

            ps.setString(1, pointsId.toString());

            try(ResultSet rs = ps.executeQuery()){
                while (rs.next()){
                    FantasyPointBreakdown fpb = FantasyPointBreakdown.builder()
                            .breakdownId(UUID.fromString(rs.getString("breakdownId")))
                            .pointsId(pointsId)
                            .ruleId(UUID.fromString(rs.getString("ruleId")))
                            .eventCount(rs.getInt("eventCount"))
                            .pointsEarned(rs.getInt("pointsEarned"))
                            .build();

                    breakdowns.add(fpb);

                }
            }

        } catch (SQLException e) {
            LOG.log(Level.SEVERE, "Could not find fantasy points breakdown by pointsId", e);
            throw new DataAccessException("Could not find fantasy points breakdown by pointsId", e);
        }

        return breakdowns;
    }
}