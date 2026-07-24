package za.ac.vzap.trytons.frontend.servlet.admin;


import jakarta.inject.Inject;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import za.ac.vzap.trytons.frontend.client.admin.AdminReportRestClient;
import za.ac.vzap.trytons.frontend.client.admin.LogRestClient;
import za.ac.vzap.trytons.frontend.client.admin.LogResponse;
import za.ac.vzap.trytons.frontend.client.admin.SystemReportRequest;
import za.ac.vzap.trytons.frontend.client.admin.SystemReportResponse;
import za.ac.vzap.trytons.frontend.servlet.shared.AbstractServlet;
import za.ac.vzap.trytons.frontend.util.report.ReportRenderer;
import java.io.IOException;
import java.io.OutputStream;
import java.util.*;

@WebServlet(name = "AdminReportServlet", urlPatterns = {"/admin/reports"})
public class AdminReportServlet extends AbstractServlet {

    private static final String VIEW = "/pages/admin-reports.jsp";

    @Inject
    private AdminReportRestClient adminReportRestClient;

    @Inject
    private LogRestClient logRestClient;

    private static final int LOG_LIMIT = 100;

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        if(!requireAdmin(request,response)) {
            return;
        }
        String viewId = request.getParameter("view");
        String downloadId = request.getParameter("download");
        // The page links to either ?view=<id> or ?download=<id>, never both, so this
        // triggers when either is present. Download wins if both somehow arrive; it is
        // the only one that sets the attachment header.
        if(viewId != null || downloadId != null) {
            streamReport(request, downloadId != null ? downloadId : viewId, downloadId != null, response);
            return;
        }
        loadReports(request);
        loadLogs(request);
        forward(request, response);
    }
    private void streamReport(HttpServletRequest request, String reportId, boolean asAttachment, HttpServletResponse response) throws IOException {
        UUID id;
        try {
            id = UUID.fromString(reportId);
        } catch (IllegalArgumentException e) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid report id");
            return;
        }
        Optional<SystemReportResponse> reportOpt = adminReportRestClient.getReportById(id);
        if(reportOpt.isEmpty()) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND, "Report not found");
            return;
        }
        SystemReportResponse report = reportOpt.get();

        if (asAttachment) {
            // Download: a rendered PDF, not raw JSON.
            response.setContentType("application/pdf");
            response.setHeader("Content-Disposition", "attachment; filename=\"" + downloadFilename(report, id) + "\"");
            try (OutputStream out = response.getOutputStream()) {
                ReportRenderer.writePdf(report, out);
            }
        } else {
            // View: a readable HTML report page in the browser, not raw JSON.
            response.setContentType("text/html;charset=UTF-8");
            response.getWriter().write(ReportRenderer.toHtml(report, request.getContextPath()));
        }
    }

    /** A friendly, filesystem-safe PDF filename derived from the report title. */
    private String downloadFilename(SystemReportResponse report, UUID id) {
        String base = report.getReportTitle();
        if (base == null || base.isBlank()) {
            base = "system-report-" + id;
        }
        String slug = base.trim().toLowerCase().replaceAll("[^a-z0-9]+", "-").replaceAll("(^-|-$)", "");
        if (slug.isBlank()) {
            slug = "system-report-" + id;
        }
        return slug + ".pdf";
    }


    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        if(!requireAdmin(request,response)) {
            return;
        }
        String reportType = request.getParameter("reportType");
        String reportTitle = request.getParameter("reportTitle");
        String season = request.getParameter("season");
        String limit = request.getParameter("limit");
        String roundId = request.getParameter("roundId");

        if(reportType == null || reportType.isBlank() || reportTitle == null || reportTitle.isBlank()) {
            request.setAttribute("error","Report type and title are required");
            loadReports(request);
            loadLogs(request);
            forward(request, response);
            return;
        }
        SystemReportRequest reportRequest = new SystemReportRequest();
        reportRequest.setReportType(reportType.trim());
        reportRequest.setReportTitle(reportTitle.trim());

        Map<String, Object> parameters = new LinkedHashMap<>();
        if(season != null && !season.isBlank()) parameters.put("season", season.trim());
        if(limit != null && !limit.isBlank()) parameters.put("limit", limit.trim());
        if(roundId != null && !roundId.isBlank()) parameters.put("roundId", roundId.trim());
        reportRequest.setParametersJson(parameters);

        Optional<SystemReportResponse> generatedReport = adminReportRestClient.generateReport(reportRequest);
        if(generatedReport.isPresent()) {
            response.sendRedirect(request.getContextPath() + "/admin/reports");
            return;
        }
        request.setAttribute("error","Unable to generate System Report");
        loadReports(request);
        loadLogs(request);
        forward(request, response);

    }

    private void loadReports(HttpServletRequest request) {
        Optional<List<SystemReportResponse>> reportsOptional = adminReportRestClient.getReports();
        if(reportsOptional.isPresent()) {
            request.setAttribute("reports", reportsOptional.get());
        }else{
            request.setAttribute("reports", List.of());
            if(request.getAttribute("error") == null) {
                request.setAttribute("error","Unable to load reports");
            }
        }
    }

    private void loadLogs(HttpServletRequest request) {
        Optional<List<LogResponse>> logsOptional = logRestClient.getRecentLogs(LOG_LIMIT);
        if(logsOptional.isPresent()) {
            request.setAttribute("logs", logsOptional.get());
        }else{
            request.setAttribute("logs", List.of());
        }
    }

    private void forward(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        request.getRequestDispatcher(VIEW).forward(request, response);
    }
}
