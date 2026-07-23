package com.vzap.trytons.dao.admin;

import com.vzap.trytons.enums.UserRole;
import com.vzap.trytons.exceptions.DataAccessException;
import com.vzap.trytons.model.admin.Admin;
import jakarta.inject.Singleton;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import com.vzap.trytons.dao.shared.BaseDAO;

@Singleton
public class AdminDAOImpl extends BaseDAO implements AdminDAO {

    private static final Logger LOG = Logger.getLogger(AdminDAOImpl.class.getName());

    @Override
    public Optional<Admin> getAdminById(UUID userId) {
        String query = "SELECT u.*, a.adminLevel FROM user u JOIN administrator a ON u.userId = a.userId WHERE u.userId = ?";
        try (Connection con = getConnection(); PreparedStatement ps = con.prepareStatement(query)) {
            ps.setString(1, userId.toString());

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    String roleValue = rs.getString("role");
                    Admin a = Admin.builder()
                            .userId(userId)
                            .email(rs.getString("email"))
                            .passwordHash(rs.getString("passwordHash"))
                            .username(rs.getString("username"))
                            .role(roleValue != null ? UserRole.valueOf(roleValue) : null)
                            .isActive(rs.getBoolean("isActive"))
                            .profilePic(rs.getString("profilePic"))
                            .registrationDate(rs.getTimestamp("registrationDate") != null ? rs.getTimestamp("registrationDate").toLocalDateTime() : null)
                            .lastLoginAt(rs.getTimestamp("last_login_at") != null ? rs.getTimestamp("last_login_at").toLocalDateTime() : null)
                            .adminLevel(rs.getInt("adminLevel"))
                            .build();

                    return Optional.of(a);
                }
            }
        } catch (SQLException e) {
            LOG.log(Level.SEVERE, "Unable to find administrator by ID.", e);
            throw new DataAccessException("Unable to find administrator by ID.", e);
        }
        return Optional.empty();
    }

    @Override
    public boolean deactivateUserAccount(UUID userId) {
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
}
