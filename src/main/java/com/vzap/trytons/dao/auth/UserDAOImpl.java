package com.vzap.trytons.dao.auth;

import com.vzap.trytons.enums.UserRole;
import com.vzap.trytons.exceptions.DataAccessException;
import com.vzap.trytons.model.auth.User;
import jakarta.inject.Singleton;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import com.vzap.trytons.dao.shared.BaseDAO;

@Singleton
public class UserDAOImpl extends BaseDAO implements UserDAO {

    private static final Logger LOG = Logger.getLogger(UserDAOImpl.class.getName());

    //Override the interface's methods:

    private User mapUser(ResultSet rs) throws SQLException {
        String roleValue = rs.getString("role");

        return User.builder()
                .userId(UUID.fromString(rs.getString("userId")))
                .email(rs.getString("email"))
                .passwordHash(rs.getString("passwordHash"))
                .username(rs.getString("username"))
                .role(roleValue != null ? UserRole.valueOf(roleValue) : null)
                .isActive(rs.getBoolean("isActive"))
                .profilePic(rs.getString("profilePic"))
                .registrationDate(rs.getTimestamp("registrationDate") != null ? rs.getTimestamp("registrationDate").toLocalDateTime() : null)
                .lastLoginAt(rs.getTimestamp("last_login_at") != null ? rs.getTimestamp("last_login_at").toLocalDateTime() : null)
                .build();
    }

    //Get user by ID:
    @Override
    public Optional<User> getUserById(UUID userId) {
        String query = "SELECT * FROM user WHERE userId = ?";
        try (Connection con = getConnection(); PreparedStatement ps = con.prepareStatement(query)) {
            ps.setString(1, userId.toString());

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapUser(rs));
                }
            }
        } catch (SQLException e) {
            LOG.log(Level.SEVERE, "Unable to find user by ID.", e);
            throw new DataAccessException("Unable to find user by ID.", e);
        }
        return Optional.empty();
    }

    //Get user by email:
    @Override
    public Optional<User> getUserByEmail(String email) {
        String query = "SELECT * FROM user WHERE email = ?";
        try (Connection con = getConnection(); PreparedStatement ps = con.prepareStatement(query)) {
            ps.setString(1, email);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapUser(rs));
                }
            }
        } catch (SQLException e) {
            LOG.log(Level.SEVERE, "Unable to find user by email.", e);
            throw new DataAccessException("Unable to find user by email.", e);
        }
        return Optional.empty();
    }

    //Get user by username:
    @Override
    public Optional<User> getUserByUsername(String username) {
        String query = "SELECT * FROM user WHERE username = ?";
        try (Connection con = getConnection(); PreparedStatement ps = con.prepareStatement(query)) {
            ps.setString(1, username);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapUser(rs));
                }
            }
        } catch (SQLException e) {
            LOG.log(Level.SEVERE, "Unable to find user by username.", e);
            throw new DataAccessException("Unable to find user by username.", e);
        }
        return Optional.empty();
    }

    //Update user:
    @Override
    public Optional<User> updateUser(User user) {
        String query = "UPDATE user SET email = ?, passwordHash = ? WHERE userId = ?";
        try (Connection con = getConnection(); PreparedStatement ps = con.prepareStatement(query)) {
            ps.setString(1, user.getEmail());
            ps.setString(2, user.getPasswordHash());
            ps.setString(3, user.getUserId().toString());

            if (ps.executeUpdate() > 0) {
                return Optional.of(user);
            }
        } catch (SQLException e) {
            LOG.log(Level.SEVERE, "Unable to update user.", e);
            throw new DataAccessException("Unable to update user.", e);
        }
        return Optional.empty();
    }

    @Override
    public boolean emailExists(String email) {
        String query = "SELECT COUNT(*) FROM user WHERE email = ?";
        try (Connection con = getConnection(); PreparedStatement ps = con.prepareStatement(query)) {
            ps.setString(1, email);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        }catch(SQLException e){
            LOG.log(Level.SEVERE, "Error checking if email exists.", e);
            throw new DataAccessException("Error checking if email exists.", e);
        }
        return false;
    }

    @Override
    public boolean usernameExists(String username) {
        String query = "SELECT COUNT(*) FROM user WHERE username = ?";
        try(Connection con = getConnection(); PreparedStatement ps = con.prepareStatement(query)){
            ps.setString(1, username);

            try(ResultSet rs = ps.executeQuery()){
                if (rs.next()){
                    return rs.getInt(1) > 0;
                }
            }
        }catch(SQLException e){
            LOG.log(Level.SEVERE, "Error checking if username exists.", e);
            throw new DataAccessException("Error checking if username exists.", e);
        }
        return false;
    }

    @Override
    public boolean updateLastLogin(UUID userId, LocalDateTime lastLoginAt) {
        String query = "UPDATE user SET last_login_at = ? WHERE userId = ?";

        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement(query)) {

            ps.setTimestamp(1, Timestamp.valueOf(lastLoginAt));
            ps.setString(2, userId.toString());

            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            LOG.log(Level.SEVERE, "Failed to update last login.", e);
            throw new DataAccessException("Failed to update last login.", e);
        }
    }

    @Override
    public List<User> searchUsers(String searchTerm) {

        String query = "SELECT * FROM user WHERE username LIKE ? OR email LIKE ?";

        List<User> searchResults = new ArrayList<>();

        if (searchTerm == null || searchTerm.isBlank()){
            return searchResults;
        } // I'm returning an empty list here if the searchTerm is null or blank

        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement(query)) {

            ps.setString(1, "%" + searchTerm + "%");
            ps.setString(2, "%" + searchTerm + "%");

            try(ResultSet rs = ps.executeQuery()){
                while (rs.next()){
                    User result = mapUser(rs);
                    searchResults.add(result);
                }
            }

        } catch (SQLException e) {
            LOG.log(Level.SEVERE, "Unable to search users.", e);
            throw new DataAccessException("Unable to search users.", e);
        }


        return searchResults;
    }

    @Override
    public boolean updateActiveStatus(UUID userId, boolean isActive) {
        String query = "UPDATE user SET isActive = ? WHERE userId = ?";

        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement(query)) {

            ps.setBoolean(1, isActive);
            ps.setString(2, userId.toString());

            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            LOG.log(Level.SEVERE, "Failed to update active status.", e);
            throw new DataAccessException("Failed to update active status.", e);
        }
    }

    @Override
    public List<User> getActiveUsers() {
        String query = "SELECT * FROM user WHERE isActive = TRUE";
        try(Connection con = getConnection();
        PreparedStatement ps = con.prepareStatement(query);
        ResultSet rs = ps.executeQuery()){
            List<User> users = new ArrayList<>();
            while (rs.next()){
                users.add(mapUser(rs));
            }
            return users;

        }catch(SQLException e){
            LOG.log(Level.SEVERE, "Unable to find active users.", e);
            throw new DataAccessException("Unable to find active users.", e);
        }
    }

    @Override
    public boolean updateProfileDetails(UUID userId, String username, String email, String profilePic) {
        String query = "UPDATE user SET username = ?, email = ?, profilePic = ? WHERE userId = ?";

        try (Connection con = getConnection(); PreparedStatement ps = con.prepareStatement(query)) {
            ps.setString(1, username);
            ps.setString(2, email);
            ps.setString(3, profilePic);
            ps.setString(4, userId.toString());

            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            LOG.log(Level.SEVERE, "Unable to update user profile details.", e);
            throw new DataAccessException("Unable to update user profile details.", e);
        }
    }

    @Override
    public boolean updatePasswordHash(UUID userId, String newPasswordHash) {
        String query = "UPDATE user SET passwordHash = ? WHERE userId = ?";

        try (Connection con = getConnection(); PreparedStatement ps = con.prepareStatement(query)) {
            ps.setString(1, newPasswordHash);
            ps.setString(2, userId.toString());

            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            LOG.log(Level.SEVERE, "Unable to update password hash.", e);
            throw new DataAccessException("Unable to update password hash.", e);
        }
    }

}
