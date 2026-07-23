package com.vzap.trytons.service.auth;

import com.vzap.trytons.dao.auth.UserDAO;
import com.vzap.trytons.dto.auth.AuthStatusResponseDTO;
import com.vzap.trytons.dto.auth.LoginResponseDTO;
import com.vzap.trytons.exceptions.AuthenticationException;
import com.vzap.trytons.exceptions.AuthorisationException;
import com.vzap.trytons.exceptions.DataAccessException;
import com.vzap.trytons.exceptions.ValidationException;
import com.vzap.trytons.model.auth.User;
import com.vzap.trytons.util.AuthTokenUtil;
import com.vzap.trytons.util.PasswordUtil;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@ApplicationScoped
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class AuthServiceImpl implements AuthService {
    @Inject
    private UserDAO userDAO;

    @Override
    public LoginResponseDTO authenticate(String identifier, String password) {

        validateCredentials(identifier, password);

        String cleanedIdentifier = identifier.trim();

        Optional<User> possibleUser = userDAO.getUserByEmail(cleanedIdentifier);

        if (possibleUser.isEmpty()) {
            possibleUser = userDAO.getUserByUsername(cleanedIdentifier);
        }

        User user = possibleUser.orElseThrow(() -> new AuthenticationException("Invalid email/username or password."));

        if (!Boolean.TRUE.equals(user.getIsActive())) {
            throw new AuthorisationException("This account is inactive.");
        }

        if (!PasswordUtil.verifyPassword(password, user.getPasswordHash())) {

            throw new AuthenticationException("Invalid email/username or password.");
        }

        boolean lastLoginUpdated = userDAO.updateLastLogin(user.getUserId(), LocalDateTime.now());

        if (!lastLoginUpdated) {
            throw new DataAccessException("Unable to update the user's last login time.", null);
        }

        String tokenCreated = AuthTokenUtil.createToken(user.getUserId());

        return new LoginResponseDTO(
                user.getUserId(),
                user.getUsername(),
                user.getEmail(),
                user.getRole(),
                tokenCreated);
    }

    @Override
    public String logout() {
        // Authentication is stateless.
        // The frontend removes its session and stored token.
        return "Logout acknowledged.";
    }

    @Override
    public AuthStatusResponseDTO getAuthStatus(String authenticatedUserId) {
        UUID userId;

        if (authenticatedUserId == null || authenticatedUserId.isBlank()) {
            throw new AuthenticationException("Authenticated user identity is unavailable.");
        }


        try {
            userId = UUID.fromString(authenticatedUserId.trim());
        } catch (IllegalArgumentException e) {
            throw new ValidationException("requestingUserId must be a valid UUID.");
        }

        User User = userDAO.getUserById(userId).orElseThrow(() -> new AuthenticationException("User not found."));

        if (!Boolean.TRUE.equals(User.getIsActive())) {
            throw new AuthenticationException("User is no longer active.");
        }
        return AuthStatusResponseDTO.builder()
                .authenticated(true)
                .userId(User.getUserId())
                .username(User.getUsername())
                .email(User.getEmail())
                .role(User.getRole())
                .build();
    }

    private void validateCredentials(String identifier, String password) {

        if (identifier == null || identifier.isBlank() || password == null || password.isBlank()) {
            throw new ValidationException("Email/username and password are required.");
        }

    }
}