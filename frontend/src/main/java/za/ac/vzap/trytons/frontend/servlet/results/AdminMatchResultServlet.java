package za.ac.vzap.trytons.frontend.servlet.results;

import jakarta.inject.Inject;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import za.ac.vzap.trytons.frontend.client.results.AdminMatchResultRestClient;
import za.ac.vzap.trytons.frontend.client.fixture.FixtureRestClient;
import za.ac.vzap.trytons.frontend.client.fixture.FixtureResponse;
import za.ac.vzap.trytons.frontend.client.results.MatchResultRequest;
import za.ac.vzap.trytons.frontend.client.results.MatchResultResponse;
import za.ac.vzap.trytons.frontend.client.results.PlayerStatisticsRequest;
import za.ac.vzap.trytons.frontend.client.results.PlayerStatisticsResponse;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import za.ac.vzap.trytons.frontend.servlet.shared.AbstractServlet;

@WebServlet(name = "AdminMatchResultServlet", urlPatterns = {"/admin/match-results"})
public class AdminMatchResultServlet extends AbstractServlet {

    private static final String VIEW = "/pages/admin-match-results.jsp";

    @Inject
    private AdminMatchResultRestClient adminMatchResultRestClient;

    @Inject
    private FixtureRestClient fixtureRestClient;

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        if(!requireAdmin(request,response)) {
            return;
        }
        loadPage(request);
        request.getRequestDispatcher(VIEW).forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        if(!requireAdmin(request,response)){
            return;
        }
        String action = request.getParameter("action");
        if (action == null) {
            action = "";
        }

        switch (action) {
            case "matchResult", "" -> submitMatchResult(request);
            case "playerStatistics" -> submitPlayerStatistics(request);
            default -> request.setAttribute("error", "Unknown capture action requested");
        }

        if (request.getAttribute("success") != null) {
            String fixtureId = request.getParameter("fixtureId");
            String redirectUrl = request.getContextPath() + "/admin/match-results"
                    + (fixtureId != null && !fixtureId.isBlank() ? "?fixtureId=" + fixtureId : "");
            response.sendRedirect(redirectUrl);
            return;
        }

        loadPage(request);
        request.getRequestDispatcher(VIEW).forward(request, response);
    }

    private void submitMatchResult(HttpServletRequest request) {
        String fixtureId = request.getParameter("fixtureId");
        Optional<UUID> fixtureUuid = parseUuid(fixtureId);

        if (fixtureUuid.isEmpty()) {
            request.setAttribute("error", "Please select a valid fixture before capturing a result");
            return;
        }
        if (authContext.getUserId() == null) {
            request.setAttribute("error", "Your session has expired, please log in again");
            return;
        }

        int teamAScore = parseNonNegativeInt(request.getParameter("teamAScore"));
        int teamBScore = parseNonNegativeInt(request.getParameter("teamBScore"));

        if (teamAScore < 0 || teamBScore < 0) {
            request.setAttribute("error", "Team scores are required and cannot be negative");
            return;
        }

        MatchResultRequest matchResultRequest = new MatchResultRequest();
        matchResultRequest.setFixtureId(fixtureUuid.get());
        matchResultRequest.setTeamAScore(teamAScore);
        matchResultRequest.setTeamBScore(teamBScore);


        Optional<MatchResultResponse> result = adminMatchResultRestClient.submitMatchResult(fixtureId, matchResultRequest);

        if (result.isPresent()) {
            request.setAttribute("success", "Match result captured successfully");
            request.setAttribute("matchResult", result.get());
        } else {
            request.setAttribute("error", "Match result could not be captured");
        }
    }

    private void submitPlayerStatistics(HttpServletRequest request) {
        String fixtureId = request.getParameter("fixtureId");
        Optional<UUID> fixtureUuid = parseUuid(fixtureId);
        Optional<UUID> resultId = parseUuid(request.getParameter("resultId"));
        Optional<UUID> teamId = parseUuid(request.getParameter("teamId"));
        Optional<UUID> playerId = parseUuid(request.getParameter("playerId"));

        if (fixtureUuid.isEmpty() || teamId.isEmpty() || playerId.isEmpty()) {
            request.setAttribute("error", "A valid fixture, team, and player are required to capture statistics");
            return;
        }

        int[] counts = {
            parseNonNegativeInt(request.getParameter("tries")),
            parseNonNegativeInt(request.getParameter("assists")),
            parseNonNegativeInt(request.getParameter("tackles")),
            parseNonNegativeInt(request.getParameter("missedTackles")),
            parseNonNegativeInt(request.getParameter("conversions")),
            parseNonNegativeInt(request.getParameter("penalties")),
            parseNonNegativeInt(request.getParameter("metersGained")),
            parseNonNegativeInt(request.getParameter("yellowCards")),
            parseNonNegativeInt(request.getParameter("redCards"))
        };

        for (int count : counts) {
            if (count < 0) {
                request.setAttribute("error", "Statistic counts cannot be negative");
                return;
            }
        }

        PlayerStatisticsRequest statisticsRequest = new PlayerStatisticsRequest();
        statisticsRequest.setResultId(resultId.orElse(null));
        statisticsRequest.setTeamId(teamId.get());
        statisticsRequest.setPlayerId(playerId.get());
        statisticsRequest.setTries(counts[0]);
        statisticsRequest.setAssists(counts[1]);
        statisticsRequest.setTackles(counts[2]);
        statisticsRequest.setMissedTackles(counts[3]);
        statisticsRequest.setConversions(counts[4]);
        statisticsRequest.setPenalties(counts[5]);
        statisticsRequest.setMetersGained(counts[6]);
        statisticsRequest.setYellowCards(counts[7]);
        statisticsRequest.setRedCards(counts[8]);

        Optional<PlayerStatisticsResponse> statistics =
                adminMatchResultRestClient.submitPlayerStatistics(fixtureId, statisticsRequest);

        if (statistics.isPresent()) {
            request.setAttribute("success", "Player statistics captured successfully");
            request.setAttribute("playerStatistics", statistics.get());
        } else {
            request.setAttribute("error", "Player statistics could not be captured");
        }
    }

    private void loadPage(HttpServletRequest request) {
        Optional<List<FixtureResponse>> fixtures = fixtureRestClient.listFixtures(null);

        if (fixtures.isPresent()) {
            request.setAttribute("fixtures", fixtures.get());
        } else {
            request.setAttribute("fixturesError", "Unable to load fixtures for capture");
            request.setAttribute("fixtures", List.of());
        }

        String fixtureId = request.getParameter("fixtureId");
        request.setAttribute("selectedFixtureId", fixtureId);

        if (fixtureId != null && !fixtureId.isBlank() && request.getAttribute("matchResult") == null) {
            Optional<MatchResultResponse> matchResult = adminMatchResultRestClient.getMatchResult(fixtureId);
            matchResult.ifPresent(result -> request.setAttribute("matchResult", result));
        }
    }


    private int parseNonNegativeInt(String value) {
        if (value == null || value.isBlank()) {
            return -1;
        }
        try {
            int parsed = Integer.parseInt(value.trim());
            return parsed < 0 ? -1 : parsed;
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    @Override
    public String getServletInfo() {
        return "Admin Match Result Servlet, handles fixture selection, match result capture, and player statistics capture";
    }
}
