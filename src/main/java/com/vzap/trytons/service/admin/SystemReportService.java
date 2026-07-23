package com.vzap.trytons.service.admin;

import com.vzap.trytons.dto.admin.SystemReportRequestDTO;
import com.vzap.trytons.dto.admin.SystemReportResponseDTO;

import java.util.List;
import java.util.UUID;

public interface SystemReportService {
    SystemReportResponseDTO generateReport(UUID actorUserId, SystemReportRequestDTO request);
    List<SystemReportResponseDTO> listReports(UUID actorUserId);
    SystemReportResponseDTO getReportById(UUID actorUserId, UUID reportId);
}
