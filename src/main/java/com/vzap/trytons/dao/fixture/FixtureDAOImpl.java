package com.vzap.trytons.dao.fixture;

import com.vzap.trytons.enums.FixtureStatus;
import com.vzap.trytons.exceptions.ConflictException;
import com.vzap.trytons.exceptions.DataAccessException;
import com.vzap.trytons.model.fixture.Fixture;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import com.vzap.trytons.dao.shared.BaseDAO;

public class FixtureDAOImpl extends BaseDAO implements FixtureDAO {
    private static final Logger LOG = Logger.getLogger(FixtureDAOImpl.class.getName());
    @Override
    public Fixture create(Fixture fixture) {
        String query = "INSERT INTO fixture (fixtureId, leagueId, roundId, team_a_id, team_b_id, fixtureDate, fixtureTime, status, simulationDate) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try(Connection con = getConnection();
            PreparedStatement ps = con.prepareStatement(query)){
            ps.setString(1,fixture.getFixtureId().toString());
            ps.setString(2,fixture.getLeagueId().toString());
            ps.setString(3,fixture.getRoundId().toString());
            ps.setString(4,fixture.getTeamAId().toString());
            ps.setString(5,fixture.getTeamBId().toString());
            ps.setDate(6,Date.valueOf(fixture.getFixtureDate()));
            ps.setTime(7,Time.valueOf(fixture.getFixtureTime()));
            ps.setString(8,fixture.getStatus().name());
            if(fixture.getSimulationDate() != null){
                ps.setTimestamp(9,Timestamp.valueOf(fixture.getSimulationDate()));
            }else{
                ps.setNull(9,Types.TIMESTAMP);
            }
            if(ps.executeUpdate() == 1){
                Optional<Fixture> createdFixture = findById(fixture.getFixtureId());
                if(createdFixture.isPresent()){
                    return createdFixture.get();
                }
                throw new DataAccessException("Fixture was inserted, but cannot be retrieved.",null);
            }
        }catch(SQLException e){
            if ("45000".equals(e.getSQLState())) {
                throw new ConflictException(
                        e.getMessage() != null
                                ? e.getMessage()
                                : "The fixture could not be created because it conflicts with an existing record."
                );
            }

            String message = e.getMessage();
            if (message != null && message.contains("uk_fixture_round_teams")) {
                throw new ConflictException("A fixture already exists for these teams in this league round.");
            }

            LOG.log(Level.SEVERE, "Unable to create fixture", e);
            throw new DataAccessException("Unable to create fixture", e);
        }
        return null;
    }

    @Override
    public Optional<Fixture> findById(UUID fixtureId) {
        String query = "SELECT * FROM fixture WHERE fixtureId = ?";
        try(Connection con = getConnection();
        PreparedStatement ps = con.prepareStatement(query)){
            ps.setString(1,fixtureId.toString());
            ResultSet rs = ps.executeQuery();
            if(rs.next()){
                return Optional.of(this.mapFixture(rs));
            }
        }catch (SQLException e){
            LOG.log(Level.SEVERE, "Unable to find fixture", e);
            throw new DataAccessException("Unable to find fixture", e);
        }
        return Optional.empty();
    }

    @Override
    public List<Fixture> findByLeagueId(UUID leagueId) {
        String query = "SELECT * FROM fixture WHERE leagueId = ?";
        List<Fixture> fixtures = new ArrayList<>();
        try(Connection con = getConnection();
        PreparedStatement ps = con.prepareStatement(query)){
            ps.setString(1,leagueId.toString());
            ResultSet rs = ps.executeQuery();
            while (rs.next()){
                fixtures.add(this.mapFixture(rs));
            }
        }catch (SQLException e){
            LOG.log(Level.SEVERE, "Unable to find fixture", e);
            throw new DataAccessException("Unable to find fixture", e);
        }
        return fixtures;
    }

    @Override
    public List<Fixture> findByRoundId(UUID roundId) {
        String query = "SELECT * FROM fixture WHERE roundId = ?";
        List<Fixture> fixtures = new ArrayList<>();
        try(Connection con = getConnection();
        PreparedStatement ps = con.prepareStatement(query)){
            ps.setString(1,roundId.toString());
            ResultSet rs = ps.executeQuery();
            while (rs.next()){
                fixtures.add(this.mapFixture(rs));
            }
        }catch(SQLException e){
            LOG.log(Level.SEVERE, "Unable to find fixture", e);
            throw new DataAccessException("Unable to find fixture", e);
        }
        return fixtures;
    }

    @Override
    public List<Fixture> findByTeamId(UUID teamId) {
        String query = "SELECT * FROM fixture WHERE team_a_id = ? OR team_b_id = ?";
        List<Fixture> fixtures = new ArrayList<>();
        try(Connection con = getConnection();
        PreparedStatement ps = con.prepareStatement(query)){
            ps.setString(1,teamId.toString());
            ps.setString(2,teamId.toString());
            ResultSet rs = ps.executeQuery();
            while (rs.next()){
                fixtures.add(this.mapFixture(rs));
            }
        }catch (SQLException e){
            LOG.log(Level.SEVERE, "Unable to find fixture", e);
            throw new DataAccessException("Unable to find fixture", e);
        }
        return fixtures;
    }

    @Override
    public List<Fixture> findByStatus(FixtureStatus status) {
        String query = "SELECT * FROM fixture WHERE status = ?";
        List<Fixture> fixtures = new ArrayList<>();
        try(Connection con = getConnection();
        PreparedStatement ps = con.prepareStatement(query)){
            ps.setString(1,status.name());
            ResultSet rs = ps.executeQuery();
            while (rs.next()){
                fixtures.add(this.mapFixture(rs));
            }
        }catch (SQLException e){
            LOG.log(Level.SEVERE, "Unable to find fixture", e);
            throw new DataAccessException("Unable to find fixture", e);
        }
        return fixtures;
    }

    @Override
    public List<Fixture> getAllFixtures() {
        String query = "SELECT * FROM fixture";
        List<Fixture> fixtures = new ArrayList<>();
        try(Connection con = getConnection();
        PreparedStatement ps = con.prepareStatement(query)){
            ResultSet rs = ps.executeQuery();
            while (rs.next()){
                fixtures.add(this.mapFixture(rs));
            }
        }catch (SQLException e){
            LOG.log(Level.SEVERE, "Unable to find fixture", e);
            throw new DataAccessException("Unable to find fixture", e);
        }
        return fixtures;
    }

    @Override
    public boolean updateFixture(Fixture fixture) {
        String query = "UPDATE fixture "
                + "SET fixtureDate = ?, fixtureTime = ?, status = ?, simulationDate = ? "
                + "WHERE fixtureId = ?";

        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement(query)) {

            ps.setDate(1, Date.valueOf(fixture.getFixtureDate()));
            ps.setTime(2, Time.valueOf(fixture.getFixtureTime()));
            ps.setString(3, fixture.getStatus().name());

            if (fixture.getSimulationDate() != null) {
                ps.setTimestamp(4, Timestamp.valueOf(fixture.getSimulationDate()));
            } else {
                ps.setNull(4, Types.TIMESTAMP);
            }

            ps.setString(5, fixture.getFixtureId().toString());

            return ps.executeUpdate() == 1;

        } catch (SQLException e) {
            if ("45000".equals(e.getSQLState())) {
                throw new ConflictException(e.getMessage() != null ? e.getMessage() : "The fixture could not be updated because it conflicts with an existing record.");
            }

            LOG.log(Level.SEVERE, "Unable to update fixture", e);
            throw new DataAccessException("Unable to update fixture", e);
        }
    }

    @Override
    public boolean cancelFixture(UUID fixtureId) {
        String query =  "UPDATE fixture SET status='CANCELLED' WHERE fixtureId=?";
        try(Connection con = getConnection();
        PreparedStatement ps = con.prepareStatement(query)){
            ps.setString(1,fixtureId.toString());
            return ps.executeUpdate() > 0;
        }catch(SQLException e){
            LOG.log(Level.SEVERE, "Unable to cancel fixture", e);
            throw new DataAccessException("Unable to cancel fixture", e);
        }
    }

    @Override
    public boolean updateStatus(Fixture fixture, FixtureStatus status) {
        String query = "UPDATE fixture SET status=? WHERE fixtureId=?";
        try(Connection con = getConnection();
        PreparedStatement ps = con.prepareStatement(query)){
            ps.setString(1,status.name());
            ps.setString(2,fixture.getFixtureId().toString());
            return ps.executeUpdate() > 0;
        }catch(SQLException e){
            LOG.log(Level.SEVERE, "Unable to update fixture", e);
            throw new DataAccessException("Unable to update fixture", e);
        }
    }
    private Fixture mapFixture(ResultSet rs) throws SQLException {
        Timestamp simulationTimestamp = rs.getTimestamp("simulationDate");
        Timestamp createdAtTimestamp = rs.getTimestamp("createdAt");

        return Fixture.builder()
                .fixtureId(UUID.fromString(rs.getString("fixtureId")))
                .leagueId(UUID.fromString(rs.getString("leagueId")))
                .roundId(UUID.fromString(rs.getString("roundId")))
                .teamAId(UUID.fromString(rs.getString("team_a_id")))
                .teamBId(UUID.fromString(rs.getString("team_b_id")))
                .fixtureDate(rs.getDate("fixtureDate").toLocalDate())
                .fixtureTime(rs.getTime("fixtureTime").toLocalTime())
                .status(FixtureStatus.valueOf(rs.getString("status")))
                .simulationDate(simulationTimestamp == null ? null : simulationTimestamp.toLocalDateTime())
                .createdAt(createdAtTimestamp == null ? null : createdAtTimestamp.toLocalDateTime())
                .build();
    }
}
