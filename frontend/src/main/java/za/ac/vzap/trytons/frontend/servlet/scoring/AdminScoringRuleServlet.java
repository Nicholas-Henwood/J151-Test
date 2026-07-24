package za.ac.vzap.trytons.frontend.servlet.scoring;

import jakarta.inject.Inject;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import za.ac.vzap.trytons.frontend.client.scoring.ScoringRuleRequest;
import za.ac.vzap.trytons.frontend.client.scoring.ScoringRuleResponse;
import za.ac.vzap.trytons.frontend.client.scoring.ScoringRuleRestClient;
import za.ac.vzap.trytons.frontend.servlet.shared.AbstractServlet;

import java.io.IOException;
import java.time.Year;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@WebServlet(name = "AdminScoringRuleServlet", urlPatterns = {"/admin/scoring-rules"})
public class AdminScoringRuleServlet extends AbstractServlet {

    private static final String VIEW = "/pages/admin-scoring-rules.jsp";

    @Inject
    private ScoringRuleRestClient scoringRuleRestClient;

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        if (!requireAdmin(request, response)) {
            return;
        }
        loadPage(request, request.getParameter("season"), request.getParameter("ruleId"));
        request.getRequestDispatcher(VIEW).forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        if (!requireAdmin(request, response)) {
            return;
        }
        String season = saveScoringRule(request);
        if (request.getAttribute("success") != null) {
            flashSuccess(request, (String) request.getAttribute("success"));
            response.sendRedirect(request.getContextPath() + "/admin/scoring-rules?season="
                    + java.net.URLEncoder.encode(season, java.nio.charset.StandardCharsets.UTF_8));
            return;
        }
        loadPage(request, season, null);
        request.getRequestDispatcher(VIEW).forward(request, response);
    }

    private String saveScoringRule(HttpServletRequest request) {
        String season = request.getParameter("season");
        String eventType = request.getParameter("eventType");
        Optional<UUID> ruleId = parseUuid(request.getParameter("ruleId"));

        if (eventType == null || eventType.isBlank() || season == null || season.isBlank()) {
            request.setAttribute("error", "Event type and season are required to save a scoring rule");
            return season;
        }

        Integer pointsAwarded = parseInt(request.getParameter("pointsAwarded"));
        if (pointsAwarded == null) {
            request.setAttribute("error", "Points awarded must be a valid whole number");
            return season;
        }

        ScoringRuleRequest scoringRuleRequest = new ScoringRuleRequest();
        ruleId.ifPresent(scoringRuleRequest::setRuleId);
        scoringRuleRequest.setEventType(eventType.trim());
        scoringRuleRequest.setPointsAwarded(pointsAwarded);
        scoringRuleRequest.setSeason(season.trim());
        scoringRuleRequest.setActive(request.getParameter("active") != null);
        scoringRuleRequest.setIsDeduction(request.getParameter("isDeduction") != null);
        scoringRuleRequest.setDescription(request.getParameter("description"));

        Optional<ScoringRuleResponse> saved = scoringRuleRestClient.saveScoringRule(scoringRuleRequest);
        if (saved.isPresent()) {
            request.setAttribute("success", "Scoring rule saved successfully");
        } else {
            // Surface the backend's specific reason (e.g. a season whose ruleset is locked
            // because it already has results) rather than an opaque generic failure.
            request.setAttribute("error", apiCallStatus.getMessage("Scoring rule could not be saved"));
        }
        return season;
    }

    private void loadPage(HttpServletRequest request, String seasonParam, String ruleIdParam) {
        String season = (seasonParam == null || seasonParam.isBlank())
                ? String.valueOf(Year.now().getValue())
                : seasonParam.trim();
        request.setAttribute("selectedSeason", season);

        Optional<List<ScoringRuleResponse>> rules = scoringRuleRestClient.getScoringRules(season);
        List<ScoringRuleResponse> scoringRules = rules.orElse(List.of());
        if (rules.isEmpty()) {
            request.setAttribute("rulesError", "Unable to load scoring rules for the selected season");
        }
        request.setAttribute("scoringRules", scoringRules);

        // Determined straight from the backend (does the season have results?) rather than
        // inferred from the rules list, so a locked season with no active rules still locks.
        boolean seasonLocked = scoringRuleRestClient.isSeasonLocked(season);
        request.setAttribute("seasonLocked", seasonLocked);

        if (ruleIdParam != null && !ruleIdParam.isBlank()) {
            for (ScoringRuleResponse rule : scoringRules) {
                if (rule.getRuleId() != null && rule.getRuleId().toString().equals(ruleIdParam.trim())) {
                    request.setAttribute("editingRule", rule);
                    break;
                }
            }
        }
    }

    private Integer parseInt(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return Integer.parseInt(value.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    @Override
    public String getServletInfo() {
        return "Admin Scoring Rule Servlet, handles season-scoped scoring rule listing and create/update capture";
    }
}
