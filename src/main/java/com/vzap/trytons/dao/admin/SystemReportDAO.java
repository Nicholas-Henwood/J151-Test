package com.vzap.trytons.dao.admin;

import com.vzap.trytons.enums.SystemReportType;
import com.vzap.trytons.model.admin.SystemReport;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface SystemReportDAO {
    SystemReport save(SystemReport systemReport);
    Optional<SystemReport> findById(UUID reportId);
    List<SystemReport> findAll();
    List<SystemReport> findByType(SystemReportType reportType);
}
