package com.vzap.trytons.model.admin;

import com.vzap.trytons.enums.SystemReportType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class SystemReport {
    private UUID reportId;
    private UUID generatedByAdminUserId;

    private SystemReportType reportType;

    private String reportTitle;

    private Map<String , Object > parametersJson;
    private Map<String , Object > resultJson;

    private LocalDateTime generatedAt;
}