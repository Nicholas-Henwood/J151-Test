package za.ac.vzap.trytons.frontend.servlet.fixture;

import jakarta.inject.Inject;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import za.ac.vzap.trytons.frontend.client.fixture.AdminFixtureRestClient;
import za.ac.vzap.trytons.frontend.client.fixture.FixtureRequest;
import za.ac.vzap.trytons.frontend.client.fixture.FixtureResponse;
import za.ac.vzap.trytons.frontend.client.fixture.FixtureRestClient;
import za.ac.vzap.trytons.frontend.client.league.LeagueMemberResponse;
import za.ac.vzap.trytons.frontend.client.league.LeagueResponse;
import za.ac.vzap.trytons.frontend.client.league.LeagueRestClient;
import za.ac.vzap.trytons.frontend.client.round.RoundResponse;
import za.ac.vzap.trytons.frontend.client.round.RoundRestClient;
import za.ac.vzap.trytons.frontend.servlet.shared.AbstractServlet;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

@WebServlet(name = "AdminFixtureServlet", urlPatterns = {"/admin/fixtures"})
public class AdminFixtureServlet extends AbstractServlet {

    private static final String VIEW = "/pages/admin-fixtures.jsp";

    private static final Logger LOG = Logger.getLogger(AdminFixtureServlet.class.getName());
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

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
            flashSuccess(request, (String) request.getAttribute("success"));
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
            // Surface the backend's specific reason (e.g. an illegal status transition)
            // rather than an opaque generic failure.
            request.setAttribute("error", apiCallStatus.getMessage("Fixture status could not be updated"));
        }
    }

    // Mirrors FixtureServiceImpl.isValidStatusTransition on the backend so the per-row
    // dropdown only offers moves the server will accept. Keep the two in sync.
    private Map<String, List<String>> statusTransitions() {
        Map<String, List<String>> transitions = new LinkedHashMap<>();
        transitions.put("UPCOMING", List.of("LOCKED", "CANCELLED"));
        transitions.put("LOCKED", List.of("SIMULATING", "CANCELLED"));
        transitions.put("SIMULATING", List.of("COMPLETED", "CANCELLED"));
        transitions.put("COMPLETED", List.of("PROCESSED"));
        transitions.put("PROCESSED", List.of());
        transitions.put("CANCELLED", List.of());
        return transitions;
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

        // Valid next-status choices per current status, so the per-row dropdown only
        // offers transitions the backend will accept (see statusTransitions()).
        request.setAttribute("statusTransitions", statusTransitions());

        Optional<List<RoundResponse>> rounds = roundRestClient.listRounds();
        request.setAttribute("rounds", rounds.orElse(List.of()));

        Optional<List<LeagueResponse>> leagues = leagueRestClient.listPublicLeagues();
        List<LeagueResponse> leagueList = leagues.orElse(List.of());
        request.setAttribute("leagues", leagueList);

        // A fixture's team_a_id/team_b_id are fantasy team ids that must be active
        // members of the fixture's own league (fk_fixture_team_a/b_membership), so
        // admins pick a team by name from that league's roster instead of typing a
        // raw team id they have no way to look up.
        Map<String, List<LeagueMemberResponse>> teamsByLeagueId = new HashMap<>();
        for (LeagueResponse league : leagues.orElse(List.of())) {
            List<LeagueMemberResponse> members = leagueRestClient.listMembers(league.getLeagueId()).orElse(List.of());
            List<LeagueMemberResponse> activeMembers = new ArrayList<>();
            for (LeagueMemberResponse member : members) {
                if (member.isActive()) {
                    activeMembers.add(member);
                }
            }
            teamsByLeagueId.put(league.getLeagueId(), activeMembers);
        }
        request.setAttribute("teamsByLeagueId", teamsByLeagueId);

        // Name lookups so the fixtures table can show a league name and round label
        // instead of raw ids. Keyed by the id's string form; the JSP looks them up
        // with ${map[fixture.leagueId.toString()]} since the fixture carries UUIDs.
        Map<String, String> leagueNames = new HashMap<>();
        leagueList.forEach(l -> leagueNames.put(l.getLeagueId(), l.getLeagueName()));
        request.setAttribute("leagueNamesById", leagueNames);

        Map<String, String> roundLabels = new HashMap<>();
        rounds.orElse(List.of()).forEach(r ->
                roundLabels.put(r.getRoundId(), r.getSeason() + " · Round " + r.getRoundNumber()));
        request.setAttribute("roundLabelsById", roundLabels);

        // Team choices for the create-fixture form, grouped by league. A fixture may
        // only pair teams that are active members of the chosen league (enforced by
        // the backend), so the form's team dropdowns are populated per league from
        // this map. Emitted as JSON for the page's client-side league->teams filter.
        request.setAttribute("teamsByLeagueJson", buildTeamsByLeagueJson(leagueList));
    }

    /**
     * Builds a {@code {"<leagueId>":[{"id","name"}], ...}} JSON string of the active
     * teams in each league, used to populate the team dropdowns when a league is
     * selected. Falls back to an empty object if serialisation fails.
     */
    private String buildTeamsByLeagueJson(List<LeagueResponse> leagues) {
        Map<String, List<Map<String, String>>> teamsByLeague = new LinkedHashMap<>();

        for (LeagueResponse league : leagues) {
            String leagueId = league.getLeagueId();
            List<Map<String, String>> teams = new ArrayList<>();

            leagueRestClient.listMembers(leagueId).ifPresent(members -> {
                for (LeagueMemberResponse member : members) {
                    if (!member.isActive() || member.getTeamId() == null || member.getTeamId().isBlank()) {
                        continue;
                    }
                    Map<String, String> team = new LinkedHashMap<>();
                    team.put("id", member.getTeamId());
                    team.put("name", member.getTeamDisplayName() != null && !member.getTeamDisplayName().isBlank()
                            ? member.getTeamDisplayName() : member.getTeamId());
                    teams.add(team);
                }
            });

            teamsByLeague.put(leagueId, teams);
        }

        try {
            return OBJECT_MAPPER.writeValueAsString(teamsByLeague);
        } catch (Exception e) {
            LOG.log(Level.WARNING, "Unable to serialise teams-by-league for the fixture form", e);
            return "{}";
        }
    }

    @Override
    public String getServletInfo() {
        return "Admin Fixture Servlet, handles fixture listing and fixture status updates";
    }
}
