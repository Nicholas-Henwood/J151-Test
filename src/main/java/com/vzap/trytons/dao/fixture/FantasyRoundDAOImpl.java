package com.vzap.trytons.dao.fixture;

import com.vzap.trytons.enums.FantasyRoundStatus;
import com.vzap.trytons.exceptions.DataAccessException;
import com.vzap.trytons.model.fixture.FantasyRound;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.vzap.trytons.dao.shared.BaseDAO;

public class FantasyRoundDAOImpl extends BaseDAO implements FantasyRoundDAO {
    private static final Logger LOG = Logger.getLogger(FantasyRoundDAOImpl.class.getName());


    @Override
    public Optional<FantasyRound> getRoundById(UUID roundId) {

        String query = "SELECT * FROM fantasyRound WHERE roundId = ?";
        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement(query)) {

            ps.setString(1, roundId.toString());

            try(ResultSet rs = ps.executeQuery()){
                if (rs.next()){
                    FantasyRound fr = FantasyRound.builder()
                            .roundId(roundId)
                            .season(rs.getString("season"))
                            .roundNumber(rs.getInt("roundNumber"))
                            .openDate(rs.getObject("openDate", LocalDateTime.class))
                            .lockDeadline(rs.getObject("lockDeadline", LocalDateTime.class))
                            .endDate(rs.getObject("endDate", LocalDateTime.class))
                            .status(FantasyRoundStatus.valueOf(rs.getString("status")))

                            .build();
                    return Optional.of(fr);
                }
            }

        } catch (SQLException e) {
            LOG.log(Level.SEVERE, "Could not find fantasy round by ID", e);
            throw new DataAccessException("Could not find fantasy round by ID", e);
        }

        return Optional.empty();
    }

    @Override
    public Optional<FantasyRound> getRoundBySeasonAndNumber(String season, int roundNumber) {

        String query = "SELECT * FROM fantasyRound WHERE season = ? AND roundNumber = ?";

        try(Connection con = getConnection();
            PreparedStatement ps = con.prepareStatement(query)) {

            ps.setString(1, season);
            ps.setInt(2, roundNumber);

            try(ResultSet rs = ps.executeQuery()){
                if (rs.next()){

                    FantasyRound fr = FantasyRound.builder()
                            .roundId(UUID.fromString(rs.getString("roundId")))
                            .season(season)
                            .roundNumber(roundNumber)
                            .openDate(rs.getObject("openDate", LocalDateTime.class))
                            .lockDeadline(rs.getObject("lockDeadline", LocalDateTime.class))
                            .endDate(rs.getObject("endDate", LocalDateTime.class))
                            .status(FantasyRoundStatus.valueOf(rs.getString("status")))

                            .build();

                    return Optional.of(fr);

                }
            }

        } catch (SQLException e) {
            LOG.log(Level.SEVERE, "Could not find fantasy round by season and round number", e);
            throw new DataAccessException("Could not find fantasy round by season and round number", e);
        }
        return Optional.empty();
    }

    @Override
    public List<FantasyRound> getAllRounds() {

        String query = "SELECT * FROM fantasyRound";

        List<FantasyRound> fantasyRounds = new ArrayList<>();

        try(Connection con = getConnection();
            PreparedStatement ps = con.prepareStatement(query)) {

            try(ResultSet rs = ps.executeQuery()){

                while (rs.next()){
                    FantasyRound fr = FantasyRound.builder()
                            .roundId(UUID.fromString(rs.getString("roundId")))
                            .season(rs.getString("season"))
                            .roundNumber(rs.getInt("roundNumber"))
                            .openDate(rs.getObject("openDate", LocalDateTime.class))
                            .lockDeadline(rs.getObject("lockDeadline", LocalDateTime.class))
                            .endDate(rs.getObject("endDate", LocalDateTime.class))
                            .status(FantasyRoundStatus.valueOf(rs.getString("status")))

                            .build();

                    fantasyRounds.add(fr);

                }
            }


        } catch (SQLException e) {
            LOG.log(Level.SEVERE, "Could not find all fantasy rounds", e);
            throw new DataAccessException("Could not find all fantasy rounds", e);
        }


        return fantasyRounds;
    }

    @Override
    public List<FantasyRound> getRoundsByStatus(FantasyRoundStatus status) {
        String query = "SELECT * FROM fantasyRound WHERE status = ?";

        List<FantasyRound> fantasyRounds = new ArrayList<>();

        try(Connection con = getConnection();
            PreparedStatement ps = con.prepareStatement(query)) {

            ps.setString(1, status.name());

            try(ResultSet rs = ps.executeQuery()){
                while (rs.next()){

                    FantasyRound fr = FantasyRound.builder()
                            .roundId(UUID.fromString(rs.getString("roundId")))
                            .season(rs.getString("season"))
                            .roundNumber(rs.getInt("roundNumber"))
                            .openDate(rs.getObject("openDate", LocalDateTime.class))
                            .lockDeadline(rs.getObject("lockDeadline", LocalDateTime.class))
                            .endDate(rs.getObject("endDate", LocalDateTime.class))
                            .status(status)
                            .build();

                    fantasyRounds.add(fr);

                }
            }

        } catch (SQLException e) {
            LOG.log(Level.SEVERE, "Could not find fantasy round by status", e);
            throw new DataAccessException("Could not find fantasy round by status", e);
        }
        return fantasyRounds;
    }

    @Override
    public Optional<FantasyRound> getCurrentOpenRound() {

        String query = "SELECT * FROM fantasyRound WHERE status = ? ORDER BY openDate DESC LIMIT 1";

        try(Connection con = getConnection();
            PreparedStatement ps = con.prepareStatement(query)) {

            ps.setString(1, FantasyRoundStatus.OPEN.name());

            try(ResultSet rs = ps.executeQuery()){
                if (rs.next()){

                    FantasyRound fr = FantasyRound.builder()
                            .roundId(UUID.fromString(rs.getString("roundId")))
                            .season(rs.getString("season"))
                            .roundNumber(rs.getInt("roundNumber"))
                            .openDate(rs.getObject("openDate", LocalDateTime.class))
                            .lockDeadline(rs.getObject("lockDeadline", LocalDateTime.class))
                            .endDate(rs.getObject("endDate", LocalDateTime.class))
                            .status(FantasyRoundStatus.OPEN)

                            .build();

                    return Optional.of(fr);

                }
            }

        } catch (SQLException e) {
            LOG.log(Level.SEVERE, "Could not get current open fantasy round", e);
            throw new DataAccessException("Could not get current open fantasy round", e);
        }
        return Optional.empty();
    }

    @Override
    public boolean updateRoundStatus(UUID roundId, FantasyRoundStatus status) {

        String query = "UPDATE fantasyRound SET status = ? WHERE roundId = ?";

        try(Connection con = getConnection();
            PreparedStatement ps = con.prepareStatement(query)) {

            ps.setString(1, status.name());
            ps.setString(2, roundId.toString());

            if(ps.executeUpdate() > 0){
                return true;
            }

        } catch (SQLException e) {
            LOG.log(Level.SEVERE, "Could not update fantasy round status", e);
            throw new DataAccessException("Could not update fantasy round status", e);
        }
        return false;
    }

    @Override
    public boolean roundExists(UUID roundId) {
        String query = "SELECT * FROM fantasyRound WHERE roundId = ?";

        try(Connection con = getConnection();
            PreparedStatement ps = con.prepareStatement(query)) {

            ps.setString(1, roundId.toString());

            try(ResultSet rs = ps.executeQuery()){
                if (rs.next()){
                    return true;
                }
            }

        } catch (SQLException e) {
            LOG.log(Level.SEVERE, "Unable to check whether round exists for ID " + roundId, e);
            throw new DataAccessException("Unable to check whether round exists for ID " + roundId, e);
        }
        return false;
    }
}
