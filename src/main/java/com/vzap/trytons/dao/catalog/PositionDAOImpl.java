package com.vzap.trytons.dao.catalog;
import com.vzap.trytons.exceptions.DataAccessException;
import com.vzap.trytons.model.catalog.Position;
import jakarta.inject.Singleton;

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
public class PositionDAOImpl extends BaseDAO implements PositionDAO {
    private static final Logger LOG = Logger.getLogger(PositionDAOImpl.class.getName());
    private Position mapRow(ResultSet rs) throws SQLException {
        Position position = new Position();
        position.setPositionId(UUID.fromString(rs.getString("positionId")));
        position.setPositionName(rs.getString("positionName"));
        position.setPositionCategory(rs.getString("positionCategory"));
        position.setMinRequired(rs.getInt("minRequired"));
        position.setMaxAllowed(rs.getInt("maxAllowed"));
        return position;
    }
    @Override
    public Optional<Position> findById(UUID positionId) {
        String query = "SELECT * FROM position WHERE positionId = ?";
        try(Connection con = getConnection();
            PreparedStatement ps = con.prepareStatement(query)){
            ps.setString(1, positionId.toString());
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return Optional.of(this.mapRow(rs));
            }
        }catch(SQLException e){
            LOG.log(Level.SEVERE, "Unable to find Position by ID", e);
            throw new DataAccessException("Unable to find Position by ID", e);
        }
        return Optional.empty();
    }

    @Override
    public Optional<Position> findByName(String positionName) {
        String query = "SELECT * FROM position WHERE positionName = ?";
        try(Connection con = getConnection();
        PreparedStatement ps = con.prepareStatement(query)){
            ps.setString(1, positionName);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return Optional.of(this.mapRow(rs));
            }
        }catch(SQLException e){
            LOG.log(Level.SEVERE, "Unable to find Position by name", e);
            throw new DataAccessException("Unable to find Position by name", e);
        }
        return Optional.empty();
    }

    @Override
    public Optional<Position> findByPositionCategory(String positionCategory) {
        String query = "SELECT * FROM position WHERE positionCategory = ?";
        try(Connection con = getConnection();
        PreparedStatement ps = con.prepareStatement(query)){
            ps.setString(1, positionCategory);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return Optional.of(this.mapRow(rs));
            }
        }catch(SQLException e){
            LOG.log(Level.SEVERE, "Unable to find Position category", e);
            throw new DataAccessException("Unable to find Position category", e);
        }
        return Optional.empty();
    }

    @Override
    public List<Position> findAllPositions() {
        List<Position> positions = new ArrayList<>();
        String query = "SELECT * FROM position";
        try(Connection con = getConnection();
        PreparedStatement ps = con.prepareStatement(query);
        ResultSet rs = ps.executeQuery()){
            while(rs.next()){
                positions.add(mapRow(rs));
            }

        }catch(SQLException e){
            LOG.log(Level.SEVERE, "Unable to retrieve Positions", e);
            throw new DataAccessException("Unable to retrieve Positions", e);
        }
        return positions;
    }

    @Override
    public boolean createPosition(Position position) {
        String query = "INSERT INTO position (positionId, positionName, positionCategory, minRequired, maxAllowed) VALUES (?, ?, ?, ?, ?)";
        try(Connection con = getConnection();
        PreparedStatement ps = con.prepareStatement(query)){
            ps.setString(1, position.getPositionId().toString());
            ps.setString(2, position.getPositionName());
            ps.setString(3, position.getPositionCategory());
            ps.setInt(4, position.getMinRequired());
            ps.setInt(5, position.getMaxAllowed());
            return ps.executeUpdate() > 0;
        }catch(SQLException e){
            LOG.log(Level.SEVERE, "Unable to create Position", e);
            throw new DataAccessException("Unable to create Position", e);
        }
    }

    @Override
    public boolean updatePosition(Position position) {
        String query = "UPDATE position SET positionName =?,positionCategory=?,minRequired=?,maxAllowed=? WHERE positionId = ?";
        try(Connection con = getConnection();
        PreparedStatement ps = con.prepareStatement(query)){
            ps.setString(1, position.getPositionName());
            ps.setString(2, position.getPositionCategory());
            ps.setInt(3, position.getMinRequired());
            ps.setInt(4, position.getMaxAllowed());
            ps.setString(5,position.getPositionId().toString());
            return ps.executeUpdate() > 0;
        }catch(SQLException e){
            LOG.log(Level.SEVERE, "Unable to update Position" , e);
            throw new DataAccessException("Unable to update Position", e);
        }
    }
    @Override
    public boolean existsByName(String positionName) {
        String query = "SELECT COUNT(*) FROM position WHERE positionName = ?";
        try(Connection con = getConnection();
        PreparedStatement ps = con.prepareStatement(query)){
            ps.setString(1,positionName);
            try(ResultSet rs = ps.executeQuery()){
                if(rs.next()){
                    return rs.getInt(1) > 0;
                }
            }
        }catch(SQLException e){
            LOG.log(Level.SEVERE, "Unable to find duplicate position", e);
            throw new DataAccessException("Unable to find duplicate position", e);
        }
        return false;
    }
}
