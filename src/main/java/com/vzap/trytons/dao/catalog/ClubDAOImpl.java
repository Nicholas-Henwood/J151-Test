package com.vzap.trytons.dao.catalog;

import com.vzap.trytons.exceptions.DataAccessException;
import com.vzap.trytons.model.catalog.Club;
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
public class ClubDAOImpl extends BaseDAO implements ClubDAO {

    private static final Logger LOG = Logger.getLogger(ClubDAOImpl.class.getName());

    private Club mapRow(ResultSet rs) throws SQLException {
        Club club = new Club();
        club.setClubId(UUID.fromString(rs.getString("clubId")));
        club.setClubName(rs.getString("clubName"));
        club.setLocation(rs.getString("location"));
        club.setHomeVenue(rs.getString("homeVenue"));
        club.setActive(rs.getBoolean("isActive"));
        return club;
    }

    @Override
    public Optional<Club> findByClubId(UUID clubId) {
        return findOne("SELECT * FROM club WHERE clubId = ?", clubId.toString());
    }

    @Override
    public Optional<Club> findByClubName(String clubName) {
        return findOne("SELECT * FROM club WHERE clubName = ?", clubName);
    }

    private Optional<Club> findOne(String sql, String value) {
        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, value);
            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next() ? Optional.of(mapRow(resultSet)) : Optional.empty();
            }
        } catch (SQLException e) {
            LOG.log(Level.SEVERE, "Unable to retrieve club.", e);
            throw new DataAccessException("Unable to retrieve club.", e);
        }
    }

    @Override
    public List<Club> findAllClubs() {
        List<Club> clubs = new ArrayList<>();
        String sql = "SELECT * FROM club ORDER BY clubName";

        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {

            while (resultSet.next()) {
                clubs.add(mapRow(resultSet));
            }
            return clubs;
        } catch (SQLException e) {
            LOG.log(Level.SEVERE, "Unable to retrieve clubs.", e);
            throw new DataAccessException("Unable to retrieve clubs.", e);
        }
    }

    @Override
    public List<Club> findByLocation(String location) {
        List<Club> clubs = new ArrayList<>();
        String sql = "SELECT * FROM club WHERE location = ? ORDER BY clubName";

        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, location);
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    clubs.add(mapRow(resultSet));
                }
            }
            return clubs;
        } catch (SQLException e) {
            LOG.log(Level.SEVERE, "Unable to retrieve clubs by location.", e);
            throw new DataAccessException("Unable to retrieve clubs by location.", e);
        }
    }

    @Override
    public Optional<Club> createClub(Club club) {
        String sql = "INSERT INTO club (clubId, clubName, location, homeVenue, isActive) VALUES (?, ?, ?, ?, ?)";

        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, club.getClubId().toString());
            statement.setString(2, club.getClubName());
            statement.setString(3, club.getLocation());
            statement.setString(4, club.getHomeVenue());
            statement.setBoolean(5, club.isActive());

            if (statement.executeUpdate() != 1) {
                throw new SQLException("Club insert affected an unexpected number of rows.");
            }
            return findByClubId(club.getClubId());
        } catch (SQLException e) {
            LOG.log(Level.SEVERE, "Unable to create club.", e);
            throw new DataAccessException("Unable to create club.", e);
        }
    }

    @Override
    public Optional<Club> updateClub(Club club) {
        String sql = "UPDATE club SET clubName = ?, location = ?, homeVenue = ?, isActive = ? WHERE clubId = ?";

        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, club.getClubName());
            statement.setString(2, club.getLocation());
            statement.setString(3, club.getHomeVenue());
            statement.setBoolean(4, club.isActive());
            statement.setString(5, club.getClubId().toString());

            if (statement.executeUpdate() == 0) {
                return Optional.empty();
            }
            return findByClubId(club.getClubId());
        } catch (SQLException e) {
            LOG.log(Level.SEVERE, "Unable to update club.", e);
            throw new DataAccessException("Unable to update club.", e);
        }
    }

    @Override
    public Optional<Club> updateStatus(UUID clubId, boolean isActive) {
        String sql = "UPDATE club SET isActive = ? WHERE clubId = ?";

        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setBoolean(1, isActive);
            statement.setString(2, clubId.toString());

            if (statement.executeUpdate() == 0) {
                return Optional.empty();
            }
            return findByClubId(clubId);
        } catch (SQLException e) {
            LOG.log(Level.SEVERE, "Unable to update club status.", e);
            throw new DataAccessException("Unable to update club status.", e);
        }
    }
}
