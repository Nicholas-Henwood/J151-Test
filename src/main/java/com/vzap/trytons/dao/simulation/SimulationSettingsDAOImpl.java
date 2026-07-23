package com.vzap.trytons.dao.simulation;

import com.vzap.trytons.exceptions.ConflictException;
import com.vzap.trytons.exceptions.DataAccessException;
import com.vzap.trytons.model.simulation.SimulationSettings;
import jakarta.inject.Singleton;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import com.vzap.trytons.dao.shared.BaseDAO;

@Singleton
public class SimulationSettingsDAOImpl extends BaseDAO implements SimulationSettingsDAO {
    private static final Logger LOG = Logger.getLogger(SimulationSettingsDAOImpl.class.getName());
    private static final String FIND_SETTINGS_BY_ID = "SELECT * FROM simulationSettings WHERE settingsId = ?";
    private static final String UPDATE_SETTINGS = "UPDATE simulationSettings" +
            " SET season = ?, player_ability_weight = ?, player_form_weight = ?, team_balance_weight = ?, random_variation_weight = ?, require_admin_approval = ?, allowResimulation = ?, maxResimulations = ?, isActive = ?, updatedAt = CURRENT_TIMESTAMP" +
            " WHERE settingsId = ?";
    private static final String INSERT_SETTINGS = "INSERT INTO simulationSettings " +
                    "(settingsId, season, player_ability_weight, player_form_weight, " +
                    "team_balance_weight, random_variation_weight, require_admin_approval, " +
                    "allowResimulation, maxResimulations, isActive) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
    private static final String FIND_ACTIVE_SETTINGS = "SELECT * FROM simulationSettings " +
                    "WHERE isActive = TRUE " +
                    "ORDER BY updatedAt DESC, createdAt DESC " +
                    "LIMIT 1";
    private static final String FIND_ALL =  "SELECT * FROM simulationSettings";
    private static final String MARK_ALL_INACTIVE = "UPDATE simulationSettings " +
            "SET isActive = FALSE, updatedAt = CURRENT_TIMESTAMP " +
            "WHERE isActive = TRUE";

    @Override
    public SimulationSettings save(SimulationSettings simulationSettings) {
        if (simulationSettings == null) {
            throw new DataAccessException("Simulation settings are required", null);
        }
        UUID settingsId = simulationSettings.getSettingsId();
        int rowsAffected;

        if (settingsId == null) {
            settingsId = UUID.randomUUID();
            simulationSettings.setSettingsId(settingsId);

            try (Connection con = getConnection();
                 PreparedStatement ps = con.prepareStatement(INSERT_SETTINGS)) {

                ps.setString(1, settingsId.toString());
                ps.setString(2, simulationSettings.getSeason());
                ps.setBigDecimal(3, simulationSettings.getPlayerAbilityWeight());
                ps.setBigDecimal(4, simulationSettings.getPlayerFormWeight());
                ps.setBigDecimal(5, simulationSettings.getTeamBalanceWeight());
                ps.setBigDecimal(6, simulationSettings.getRandomVariationWeight());
                ps.setBoolean(7, simulationSettings.getRequireAdminApproval());
                ps.setBoolean(8, simulationSettings.getAllowResimulation());
                ps.setInt(9, simulationSettings.getMaxResimulations());
                ps.setBoolean(10, simulationSettings.getIsActive());
                rowsAffected = ps.executeUpdate();

            } catch (SQLException e) {
                LOG.log(Level.SEVERE, "Save simulation settings failed", e);
                throw new DataAccessException("Save simulation settings failed", e);
            }
        } else {
            try (Connection con = getConnection();
                 PreparedStatement ps = con.prepareStatement(UPDATE_SETTINGS)) {
                ps.setString(1, simulationSettings.getSeason());
                ps.setBigDecimal(2, simulationSettings.getPlayerAbilityWeight());
                ps.setBigDecimal(3, simulationSettings.getPlayerFormWeight());
                ps.setBigDecimal(4, simulationSettings.getTeamBalanceWeight());
                ps.setBigDecimal(5, simulationSettings.getRandomVariationWeight());
                ps.setBoolean(6, simulationSettings.getRequireAdminApproval());
                ps.setBoolean(7, simulationSettings.getAllowResimulation());
                ps.setInt(8, simulationSettings.getMaxResimulations());
                ps.setBoolean(9, simulationSettings.getIsActive());
                ps.setString(10, settingsId.toString());

                rowsAffected = ps.executeUpdate();

            } catch (SQLException e) {
                if ("45000".equals(e.getSQLState())) {
                    throw new ConflictException(e.getMessage() != null ? e.getMessage() : "The simulation settings could not be updated because it conflicts with an existing record.");
                }

                LOG.log(Level.SEVERE, "Save simulation settings failed", e);
                throw new DataAccessException("Save simulation settings failed", e);
            }
        }

        if (rowsAffected != 1) {
            throw new DataAccessException("Save simulation settings failed", null);
        }
        UUID savedSettingsId = settingsId;
        return findById(savedSettingsId).orElseThrow(() -> new DataAccessException("Saved simulation settings could not be found", null));
    }

    @Override
    public Optional<SimulationSettings> findById(UUID settingsId) {
        if (settingsId == null) {
            return Optional.empty();
        }
        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement(FIND_SETTINGS_BY_ID)) {
            ps.setString(1, settingsId.toString());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(settingsMapper(rs));
                }
            }
        } catch (SQLException e) {
            LOG.log(Level.SEVERE, "Failure finding simulation settings by ID", e);
            throw new DataAccessException("Failure finding simulation settings by ID", e);
        }
        return Optional.empty();
    }

    @Override
    public Optional<SimulationSettings> findActive() {
        try(Connection con = getConnection();
        PreparedStatement ps = con.prepareStatement(FIND_ACTIVE_SETTINGS)) {
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(settingsMapper(rs));
                }
            }
        }catch (SQLException e){
            LOG.log(Level.SEVERE, "Finding active settings failed.", e);
            throw new DataAccessException("Finding active settings failed.", e);
        }
        return  Optional.empty();
    }

    @Override
    public List<SimulationSettings> findAll() {
        List<SimulationSettings> settings = new ArrayList<>();
        try (Connection con = getConnection();
        PreparedStatement ps = con.prepareStatement(FIND_ALL)) {
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    settings.add(settingsMapper(rs));
                }
            }
        }catch (SQLException e){
            LOG.log(Level.SEVERE, "Finding all settings failed.", e);
            throw new DataAccessException("Finding all settings failed.", e);
        }
    return settings;
    }

    @Override
    public int markAllSettingsInactive() {
        try(Connection con = getConnection();
        PreparedStatement ps = con.prepareStatement(MARK_ALL_INACTIVE)) {
            return ps.executeUpdate();
        }catch (SQLException e){
            LOG.log(Level.SEVERE, "Marking all settings inactive failed.", e);
            throw new DataAccessException("Marking all settings inactive failed.", e);
        }
    }

    private SimulationSettings settingsMapper(ResultSet rs) {

        try {
            Timestamp updatedAt = rs.getTimestamp("updatedAt");
            return SimulationSettings.builder()
                    .settingsId(UUID.fromString(rs.getString("settingsId")))
                    .season(rs.getString("season"))
                    .playerAbilityWeight(rs.getBigDecimal("player_ability_weight"))
                    .playerFormWeight(rs.getBigDecimal("player_form_weight"))
                    .teamBalanceWeight(rs.getBigDecimal("team_balance_weight"))
                    .randomVariationWeight(rs.getBigDecimal("random_variation_weight"))
                    .requireAdminApproval(rs.getBoolean("require_admin_approval"))
                    .allowResimulation(rs.getBoolean("allowResimulation"))
                    .maxResimulations(rs.getInt("maxResimulations"))
                    .isActive(rs.getBoolean("isActive"))
                    .createdAt(rs.getTimestamp("createdAt").toLocalDateTime())
                    .updatedAt(updatedAt == null ? null : updatedAt.toLocalDateTime())
                    .build();
        }catch (SQLException e){
            LOG.log(Level.WARNING, "Error thrown while building SimulationSetting in implementation class", e);
            throw new DataAccessException("Unable to build simulation settings", e);
        }

    }
}