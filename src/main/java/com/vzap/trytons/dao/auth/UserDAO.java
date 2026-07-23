package com.vzap.trytons.dao.auth;

import com.vzap.trytons.model.auth.User;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserDAO {

    Optional<User> getUserById(UUID userId);
    Optional<User> getUserByEmail(String email);
    Optional<User> getUserByUsername(String username);
    Optional<User> updateUser(User newUser);
    boolean emailExists(String email);
    boolean usernameExists(String username);
    boolean updateLastLogin(UUID userId, LocalDateTime lastLoginAt);
    List<User> searchUsers(String searchTerm);
    boolean updateActiveStatus(UUID userId, boolean isActive);
    List<User> getActiveUsers();
    boolean updateProfileDetails(UUID userId, String username, String email, String profilePic);
    boolean updatePasswordHash(UUID userId, String newPasswordHash);
}
