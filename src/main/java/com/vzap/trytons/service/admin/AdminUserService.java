package com.vzap.trytons.service.admin;

import com.vzap.trytons.dto.admin.AdminUserSearchResponseDTO;
import com.vzap.trytons.dto.admin.AdminUserStatusRequestDTO;
import com.vzap.trytons.dto.admin.AdminUserStatusResponseDTO;

import java.util.List;
import java.util.UUID;

public interface AdminUserService {
    public List<AdminUserSearchResponseDTO> searchUsers(UUID actorUserId, String searchTerm);
    public AdminUserStatusResponseDTO updateUserStatus(UUID actorUserId, UUID targetUserId, AdminUserStatusRequestDTO request);
}
