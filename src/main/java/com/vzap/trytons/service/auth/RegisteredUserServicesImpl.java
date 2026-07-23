package com.vzap.trytons.service.auth;

import com.vzap.trytons.dao.auth.RegisteredUserDAO;
import com.vzap.trytons.dao.auth.UserDAO;
import com.vzap.trytons.dto.auth.ChangePasswordRequestDTO;
import com.vzap.trytons.dto.auth.ProfileResponseDTO;
import com.vzap.trytons.dto.auth.ProfileUpdateRequestDTO;
import com.vzap.trytons.dto.auth.RegisteredUserRequestDTO;
import com.vzap.trytons.enums.RegistrationStatus;
import com.vzap.trytons.enums.UserRole;
import com.vzap.trytons.exceptions.BusinessRuleException;
import com.vzap.trytons.exceptions.ConflictException;
import com.vzap.trytons.exceptions.DataAccessException;
import com.vzap.trytons.exceptions.ResourceNotFoundException;
import com.vzap.trytons.model.auth.RegisteredUser;
import com.vzap.trytons.model.auth.User;
import com.vzap.trytons.util.PasswordUtil;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.time.LocalDateTime;
import java.util.UUID;

@ApplicationScoped
public class RegisteredUserServicesImpl implements RegisteredUserServices {
    @Inject
    private UserDAO userDAO;

    @Inject
    private RegisteredUserDAO registeredUserDAO;

    @Override
    public RegisteredUser registerUser(RegisteredUserRequestDTO userRequest) {
        String email = userRequest.getEmail();
        String username = userRequest.getUsername();
        String rawPassword = userRequest.getRawPassword();

        if (userDAO.emailExists(email)){
            throw new ConflictException("Email is already in use.");
        }
        if (userDAO.usernameExists(username)) {
            throw new ConflictException("Username is already being used.");
        }
        RegisteredUser newUser= new RegisteredUser();

        newUser.setUserId(UUID.randomUUID());
        newUser.setEmail(email);
        newUser.setUsername(username);
        newUser.setRegistrationDate(LocalDateTime.now());
        newUser.setPasswordHash(PasswordUtil.hashPassword(rawPassword));
        newUser.setRole(UserRole.REGISTERED_USER);
        newUser.setRegistrationStatus(RegistrationStatus.PENDING);
        newUser.setIsActive(true);

        return registeredUserDAO.register(newUser).orElseThrow(() -> new DataAccessException("Failed to register user.", null));
    }

    @Override
    public ProfileResponseDTO getProfile(UUID userId) {
        RegisteredUser registeredUser = registeredUserDAO.getRegisteredUserById(userId).orElseThrow(() -> new ResourceNotFoundException("User not found."));
        return toProfileResponse(registeredUser);
    }

    @Override
    public ProfileResponseDTO updateProfile(UUID actorUserId, ProfileUpdateRequestDTO request) {
        RegisteredUser currentUser = registeredUserDAO.getRegisteredUserById(actorUserId).orElseThrow(() -> new ResourceNotFoundException("User not found."));

        String newUsername = currentUser.getUsername();
        if (request.getUsername() != null && !request.getUsername().equals(currentUser.getUsername())) {
            if (userDAO.usernameExists(request.getUsername())) {
                throw new ConflictException("Username is already being used.");
            }
            newUsername = request.getUsername();
        }

        String newEmail = currentUser.getEmail();
        if (request.getEmail() != null && !request.getEmail().equals(currentUser.getEmail())) {
            if (userDAO.emailExists(request.getEmail())) {
                throw new ConflictException("Email is already in use.");
            }
            newEmail = request.getEmail();
        }

        String newProfilePic = request.getProfilePic() != null ? request.getProfilePic() : currentUser.getProfilePic();

        if (!userDAO.updateProfileDetails(actorUserId, newUsername, newEmail, newProfilePic)) {
            throw new DataAccessException("Failed to update profile.", null);
        }

        currentUser.setUsername(newUsername);
        currentUser.setEmail(newEmail);
        currentUser.setProfilePic(newProfilePic);

        return toProfileResponse(currentUser);
    }

    @Override
    public void changePassword(UUID actorUserId, ChangePasswordRequestDTO request) {
        User user = userDAO.getUserById(actorUserId).orElseThrow(() -> new ResourceNotFoundException("User not found."));

        if (!PasswordUtil.verifyPassword(request.getCurrentPassword(), user.getPasswordHash())) {
            throw new BusinessRuleException("The current password is incorrect.");
        }

        String newHash = PasswordUtil.hashPassword(request.getNewPassword());
        if (!userDAO.updatePasswordHash(actorUserId, newHash)) {
            throw new DataAccessException("Failed to update password.", null);
        }
    }

    private ProfileResponseDTO toProfileResponse(RegisteredUser registeredUser) {
        return ProfileResponseDTO.builder()
                .userId(registeredUser.getUserId())
                .email(registeredUser.getEmail())
                .username(registeredUser.getUsername())
                .role(registeredUser.getRole())
                .isActive(registeredUser.getIsActive())
                .profilePic(registeredUser.getProfilePic())
                .registrationDate(registeredUser.getRegistrationDate())
                .lastLoginAt(registeredUser.getLastLoginAt())
                .registrationStatus(registeredUser.getRegistrationStatus())
                .build();
    }
}
