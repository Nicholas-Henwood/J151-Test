package com.vzap.trytons.service.admin;

import com.vzap.trytons.dto.admin.LogResponseDTO;

import java.util.List;
import java.util.UUID;

public interface LogService {
    List<LogResponseDTO> findRecentLogs(UUID actorUserId, int limit);
}
