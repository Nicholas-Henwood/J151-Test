package com.vzap.trytons.dto.admin;

import com.vzap.trytons.enums.SystemReportType;
import lombok.*;

import java.util.Map;

@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
@Builder
public class SystemReportRequestDTO {
    private SystemReportType reportType;

    private String reportTitle;

    private Map<String , Object > parametersJson;
}
