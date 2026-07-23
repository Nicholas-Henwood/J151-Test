package za.ac.vzap.trytons.frontend.servlet.fixture;

import jakarta.inject.Inject;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import za.ac.vzap.trytons.frontend.client.fixture.AdminFixtureRestClient;
import za.ac.vzap.trytons.frontend.client.fixture.FixtureRequest;
import za.ac.vzap.trytons.frontend.client.fixture.FixtureResponse;
import za.ac.vzap.trytons.frontend.client.fixture.FixtureRestClient;
import za.ac.vzap.trytons.frontend.client.league.LeagueResponse;
import za.ac.vzap.trytons.frontend.client.league.LeagueRestClient;
import za.ac.vzap.trytons.frontend.client.round.RoundResponse;
import za.ac.vzap.trytons.frontend.client.round.RoundRestClient;
import za.ac.vzap.trytons.frontend.servlet.shared.AbstractServlet;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@WebServlet(name = "AdminFixtureServlet", urlPatterns = {"/admin/fixtures"})
public class AdminFixtureServlet extends AbstractServlet {

    private static final String VIEW = "/pages/admin-fixtures.jsp";

    @Inject
    private AdminFixtureRestClient adminFixtureRestClient;

    @Inject
    private FixtureRestClient fixtureRestClient;

    @Inject
    private RoundRestClient roundRestClient;

    @Inject
    private LeagueRestClient leagueRestClient;

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        if (!requireAdmin(request, response)) {
            return;
        }
        loadPage(request, request.getParameter("status"));
        request.getRequestDispatcher(VIEW).forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        if (!requireAdmin(request, response)) {
            return;
        }
        if ("create-fixture".equals(request.getParameter("submit"))) {
            createFixture(request);
        } else {
            updateFixtureStatus(request);
        }
        if (request.getAttribute("success") != null) {
            response.sendRedirect(request.getContextPath() + "/admin/fixtures");
            return;
        }

        loadPage(request, null);
        request.getRequestDispatcher(VIEW).forward(request, response);
    }

    private void updateFixtureStatus(HttpServletRequest request) {
        String fixtureId = request.getParameter("fixtureId");
        String status = request.getParameter("status");

        if (fixtureId == null || fixtureId.isBlank() || status == null || status.isBlank()) {
            request.setAttribute("error", "A valid fixture and status are required to update a fixture");
            return;
        }

        Optional<FixtureResponse> updated = adminFixtureRestClient.updateFixtureStatus(fixtureId.trim(), status.trim());
        if (updated.isPresent()) {
            request.setAttribute("success", "Fixture status updated successfully");
        } else {
            request.setAttribute("error", "Fixture status could not be updated");
        }
    }


    private void createFixture(HttpServletRequest request) {
        Optional<UUID> leagueId = parseUuid(request.getParameter("leagueId"));
        Optional<UUID> roundId = parseUuid(request.getParameter("roundId"));
        Optional<UUID> teamAId = parseUuid(request.getParameter("teamAId"));
        Optional<UUID> teamBId = parseUuid(request.getParameter("teamBId"));
        String fixtureDateParam = request.getParameter("fixtureDate");
        String fixtureTimeParam = request.getParameter("fixtureTime");

        if (leagueId.isEmpty() || roundId.isEmpty() || teamAId.isEmpty() || teamBId.isEmpty()
                || fixtureDateParam == null || fixtureDateParam.isBlank()
                || fixtureTimeParam == null || fixtureTimeParam.isBlank()) {
            request.setAttribute("error", "A league, round, both teams, and a fixture date/time are required to create a fixture");
            return;
        }

        try {
            LocalDate fixtureDate = LocalDate.parse(fixtureDateParam.trim());
            LocalTime fixtureTime = LocalTime.parse(fixtureTimeParam.trim());

            FixtureRequest fixtureRequest = new FixtureRequest(leagueId.get(), roundId.get(), teamAId.get(),
                    teamBId.get(), fixtureDate, fixtureTime, "UPCOMING");

            Optional<FixtureResponse> created = adminFixtureRestClient.createFixture(fixtureRequest);
            if (created.isPresent()) {
                request.setAttribute("success", "Fixture created successfully");
            } else {
                request.setAttribute("error", "Fixture could not be created");
            }
        } catch (DateTimeParseException e) {
            request.setAttribute("error", "Fixture date/time could not be understood");
        }
    }

    private void loadPage(HttpServletRequest request, String statusFilter) {
        Optional<List<FixtureResponse>> fixtures = fixtureRestClient.listFixtures(statusFilter);
        if (fixtures.isPresent()) {
            request.setAttribute("fixtures", fixtures.get());
        } else {
            request.setAttribute("fixturesError", "Unable to load fixtures");
            request.setAttribute("fixtures", List.of());
        }
        request.setAttribute("statusFilter", statusFilter);

        Optional<List<RoundResponse>> rounds = roundRestClient.listRounds();
        request.setAttribute("rounds", rounds.orElse(List.of()));

        Optional<List<LeagueResponse>> leagues = leagueRestClient.listPublicLeagues();
        request.setAttribute("leagues", leagues.orElse(List.of()));
    }

    @Override
    public String getServletInfo() {
        return "Admin Fixture Servlet, handles fixture listing and fixture status updates";
    }
}
