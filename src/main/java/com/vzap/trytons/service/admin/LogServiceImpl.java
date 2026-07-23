package com.vzap.trytons.service.admin;

import com.vzap.trytons.dao.admin.LogDAO;
import com.vzap.trytons.dao.auth.UserDAO;
import com.vzap.trytons.dto.admin.LogResponseDTO;
import com.vzap.trytons.enums.UserRole;
import com.vzap.trytons.exceptions.AuthorisationException;
import com.vzap.trytons.exceptions.ValidationException;
import com.vzap.trytons.model.admin.Log;
import com.vzap.trytons.model.auth.User;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@ApplicationScoped
public class LogServiceImpl implements LogService {
    @Inject
    private LogDAO logDAO;
    @Inject
    private UserDAO userDAO;

    @Override
    public List<LogResponseDTO> findRecentLogs(UUID actorUserId, int limit) {
        requireAdmin(actorUserId);

        int effectiveLimit = limit > 0 ? limit : 100;

        List<LogResponseDTO> responses = new ArrayList<>();
        for (Log log : logDAO.findRecentLogs(effectiveLimit)) {
            responses.add(mapToResponse(log));
        }

        return responses;
    }

    private void requireAdmin(UUID actorUserId) {
        if (actorUserId == null) {
            throw new ValidationException("An authenticated administrator is required.");
        }
        Optional<User> userOptional = userDAO.getUserById(actorUserId);

        if (userOptional.isEmpty()) {
            throw new AuthorisationException("An authenticated administrator is required.");
        }

        User user = userOptional.get();

        if (user.getRole() != UserRole.ADMINISTRATOR) {
            throw new AuthorisationException("Only admins can perform this action.");
        }
    }

    private LogResponseDTO mapToResponse(Log log) {
        return LogResponseDTO.builder()
                .logId(log.getLogId())
                .entityType(log.getEntityType())
                .entityId(log.getEntityId())
                .actionType(log.getActionType())
                .description(log.getDescription())
                .createdAt(log.getCreatedAt())
                .ipAddress(log.getIpAddress())
                .notificationId(log.getNotificationId())
                .userId(log.getUserId())
                .transferId(log.getTransferId())
                .build();
    }
}
