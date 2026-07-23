package com.vzap.trytons.dao.catalog;

import com.vzap.trytons.dao.shared.BaseDAO;
import com.vzap.trytons.enums.AvailabilityStatus;
import com.vzap.trytons.exceptions.DataAccessException;
import com.vzap.trytons.model.catalog.PlayerAvailability;
import jakarta.inject.Singleton;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

@Singleton
public class PlayerAvailabilityDAOImpl extends BaseDAO implements PlayerAvailabilityDAO {

    private static final Logger LOG = Logger.getLogger(PlayerAvailabilityDAOImpl.class.getName());

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
    public Optional<PlayerAvailability> getCurrentByPlayer(UUID playerId) {
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
    public PlayerAvailability upsert(PlayerAvailability availability) {
        UUID availabilityId = availability.getAvailabilityId() != null ? availability.getAvailabilityId() : UUID.randomUUID();

        String query = "INSERT INTO playerAvailability (availabilityId, playerId, status, effectiveDate, endDate, notes) "
                        + "VALUES (?, ?, ?, ?, ?, ?)";

        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement(query)) {

            ps.setString(1, availabilityId.toString());
            ps.setString(2, availability.getPlayerId().toString());
            ps.setString(3, availability.getStatus().name());
            ps.setDate(4, availability.getEffectiveDate() != null ? Date.valueOf(availability.getEffectiveDate()) : null);
            ps.setDate(5, availability.getEndDate() != null ? Date.valueOf(availability.getEndDate()) : null);
            ps.setString(6, availability.getNotes());

            if (ps.executeUpdate() == 1) {
                availability.setAvailabilityId(availabilityId);
                return availability;
            }

            throw new DataAccessException("Unable to persist player availability.", null);

        } catch (SQLException e) {
            LOG.log(Level.SEVERE, "Unable to persist player availability.", e);
            throw new DataAccessException("Unable to persist player availability.", e);
        }
    }
}
