package za.ac.vzap.trytons.frontend.client.admin;

import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

@Getter
@Setter
public class SystemReportResponse {
    private UUID reportId;
    private String reportType;
    private String reportTitle;
    private Map<String , Object > parametersJson;
    private Map<String , Object > resultJson;
    private LocalDateTime generatedAt;
    private UUID generatedByAdminUserId;
}
