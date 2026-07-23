package za.ac.vzap.trytons.frontend.client.admin;

import lombok.Getter;
import lombok.Setter;
import java.util.Map;

@Getter
@Setter
public class SystemReportRequest {
    private String reportType;
    private String reportTitle;
    private Map<String , Object > parametersJson;
}
