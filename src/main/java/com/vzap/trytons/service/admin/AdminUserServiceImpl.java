package com.vzap.trytons.service.admin;

import com.vzap.trytons.dao.auth.UserDAO;
import com.vzap.trytons.dto.admin.AdminUserSearchResponseDTO;
import com.vzap.trytons.dto.admin.AdminUserStatusRequestDTO;
import com.vzap.trytons.dto.admin.AdminUserStatusResponseDTO;
import com.vzap.trytons.enums.UserRole;
import com.vzap.trytons.exceptions.*;
import com.vzap.trytons.model.auth.User;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@ApplicationScoped
public class AdminUserServiceImpl implements AdminUserService{
    @Inject
    UserDAO userDAO;

    @Override
    public List<AdminUserSearchResponseDTO> searchUsers(UUID actorUserId, String searchTerm) {
        requireAdmin(actorUserId);

        List<User> users = userDAO.searchUsers(searchTerm);
        List<AdminUserSearchResponseDTO> searchResponses = new ArrayList<>();

        for (User user : users){
            AdminUserSearchResponseDTO response = mapToSearchResponse(user);
            searchResponses.add(response);
        }

        return searchResponses;
    }

    @Override
    public AdminUserStatusResponseDTO updateUserStatus(UUID actorUserId, UUID targetUserId, AdminUserStatusRequestDTO request) {

        requireAdmin(actorUserId);
        if (request.getIsActive() == null) { throw new ValidationException("isActive is required."); }
        requireAdmin(actorUserId);

        if (actorUserId.equals(targetUserId)){
            throw new BusinessRuleException("An administrator cannot change their own active status");
        }

        User target = userDAO.getUserById(targetUserId).orElseThrow(() -> new ResourceNotFoundException("User was not found."));

        boolean updated = userDAO.updateActiveStatus(targetUserId, request.getIsActive());

        if (!updated){
            throw new DataAccessException("Failed to update target user status", null);
        }

        target.setIsActive(request.getIsActive());

        return mapToStatusResponse(target);

    }

    private void requireAdmin(UUID actorUserId) {
        if (actorUserId == null) {
            throw new AuthorisationException("An authenticated administrator is required.");
        }
        User actor = userDAO.getUserById(actorUserId).orElseThrow(() -> new AuthorisationException("An authenticated administrator is required."));
        if (actor.getRole() != UserRole.ADMINISTRATOR) {
            throw new AuthorisationException("Only administrators may manage user accounts.");
        }
    }

    private AdminUserSearchResponseDTO mapToSearchResponse(User user) {
        return  AdminUserSearchResponseDTO.builder().userId(user.getUserId())
                .email(user.getEmail())
                .username(user.getUsername())
                .role(user.getRole())
                .isActive(user.getIsActive())
                .build();
    }

    private AdminUserStatusResponseDTO mapToStatusResponse(User user) {
        return AdminUserStatusResponseDTO.builder()
                .userId(user.getUserId())
                .email(user.getEmail())
                .username(user.getUsername())
                .role(user.getRole())
                .isActive(user.getIsActive())
                .build();
    }
}
