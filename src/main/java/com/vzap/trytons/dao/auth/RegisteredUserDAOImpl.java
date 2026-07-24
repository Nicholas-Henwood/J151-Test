package com.vzap.trytons.dao.auth;

import com.vzap.trytons.exceptions.ConflictException;
import com.vzap.trytons.enums.RegistrationStatus;
import com.vzap.trytons.enums.UserRole;
import com.vzap.trytons.exceptions.DataAccessException;
import com.vzap.trytons.model.auth.RegisteredUser;
import jakarta.inject.Singleton;
import java.sql.*;
import java.util.Optional;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import com.vzap.trytons.dao.shared.BaseDAO;

@Singleton
public class RegisteredUserDAOImpl extends BaseDAO implements RegisteredUserDAO {

    private static final Logger LOG = Logger.getLogger(RegisteredUserDAOImpl.class.getName());

    @Override
    public Optional<RegisteredUser> getRegisteredUserById(UUID userId) {
        // LEFT JOIN: administrators exist in `user` but have no `registeredUser` row
        // (that table holds player registration data), so an inner join hid their
        // profile entirely and returned "User not found".
        String query = "SELECT u.*, ru.registrationStatus FROM user u LEFT JOIN registeredUser ru ON u.userId = ru.userId WHERE u.userId = ?";
        try (Connection con = getConnection(); PreparedStatement ps = con.prepareStatement(query)) {
            ps.setString(1, userId.toString());

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    String roleValue = rs.getString("role");
                    RegisteredUser ru = RegisteredUser.builder()
                            .userId(userId)
                            .email(rs.getString("email"))
                            .passwordHash(rs.getString("passwordHash"))
                            .username(rs.getString("username"))
                            .role(roleValue != null ? UserRole.valueOf(roleValue) : null)
                            .isActive(rs.getBoolean("isActive"))
                            .profilePic(rs.getString("profilePic"))
                            .registrationDate(rs.getTimestamp("registrationDate") != null ? rs.getTimestamp("registrationDate").toLocalDateTime() : null)
                            .lastLoginAt(rs.getTimestamp("last_login_at") != null ? rs.getTimestamp("last_login_at").toLocalDateTime() : null)
                            .registrationStatus(rs.getString("registrationStatus") != null
                                    ? RegistrationStatus.valueOf(rs.getString("registrationStatus")) : null)
                            .build();

                    return Optional.of(ru);
                }
            }
        } catch (SQLException e) {
            LOG.log(Level.SEVERE, "Unable to find user by ID.", e);
            throw new DataAccessException("Unable to find user by ID.", e);
        }
        return Optional.empty();
    }

    @Override
    public Optional<RegisteredUser> updateProfile(RegisteredUser registeredUser) {
        String query = "UPDATE user SET profilePic = ? WHERE userId = ?";
        try (Connection con = getConnection(); PreparedStatement ps = con.prepareStatement(query)) {
            ps.setString(1, registeredUser.getProfilePic());
            ps.setString(2, registeredUser.getUserId().toString());

            if (ps.executeUpdate() > 0) {
                return Optional.of(registeredUser);
            }
        } catch (SQLException e) {
            LOG.log(Level.SEVERE, "Unable to update user profile.", e);
            throw new DataAccessException("Unable to update user profile.", e);
        }
        return Optional.empty();
    }

    @Override
    public boolean deactivateAccount(UUID userId) {
        String query = "UPDATE user SET isActive = false WHERE userId = ?";
        try (Connection con = getConnection(); PreparedStatement ps = con.prepareStatement(query)) {
            ps.setString(1, userId.toString());

            if (ps.executeUpdate() > 0) {
                return true;
            }
        } catch (SQLException e) {
            LOG.log(Level.SEVERE, "Unable to deactivate account.", e);
            throw new DataAccessException("Unable to deactivate account.", e);
        }
        return false;
    }

    @Override
    public Optional<RegisteredUser> register(RegisteredUser newUser) {
        String persistUser = "INSERT INTO user (userId, email, passwordHash, username, role, isActive, profilePic) " + "VALUES (?, ?, ?, ?, ?, ?, ?)";
        String persistRegisteredUser = "INSERT INTO registeredUser (userId, registrationStatus) VALUES (?, ?)";

            try(Connection connection = getConnection()){
                connection.setAutoCommit(false);

                try (PreparedStatement userPs = connection.prepareStatement(persistUser);
                        PreparedStatement registeredPs = connection.prepareStatement(persistRegisteredUser)) {

                    userPs.setString(1, newUser.getUserId().toString());
                    userPs.setString(2, newUser.getEmail());
                    userPs.setString(3, newUser.getPasswordHash());
                    userPs.setString(4, newUser.getUsername());
                    userPs.setString(5, newUser.getRole().name());
                    userPs.setBoolean(6, newUser.getIsActive());
                    userPs.setString(7, newUser.getProfilePic());
                    if (userPs.executeUpdate() != 1) {
                        throw new SQLException("User insert failed");
                    }

                    registeredPs.setString(1, newUser.getUserId().toString());
                    registeredPs.setString(2, newUser.getRegistrationStatus().name());
                    if (registeredPs.executeUpdate() != 1) {
                        throw  new SQLException("Registered user insert failed");
                    }
                    connection.commit();
                    return Optional.of(newUser);

                }catch (SQLException e) {
                    try {
                        connection.rollback();
                    }catch (SQLException rollbackException) {
                        e.addSuppressed(rollbackException);
                    }
                    throw e;
                }
                finally {
                    connection.setAutoCommit(true);
                }

            } catch (SQLException e) {
                LOG.log(Level.SEVERE, "Unable to register user.", e);
                    String exceptionMessage = e.getMessage();

                    if (exceptionMessage != null && exceptionMessage.contains("uk_user_email")) {
                        throw new ConflictException("Email is already in use.");
                    }
                    if (exceptionMessage != null && exceptionMessage.contains("uk_user_username")) {
                        throw new ConflictException("Username is already being used.");
                    }

                throw new DataAccessException("Unable to register user.", e);
            }
    }
}
