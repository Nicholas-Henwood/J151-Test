package za.ac.vzap.trytons.frontend.servlet.fixture;

import jakarta.inject.Inject;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import za.ac.vzap.trytons.frontend.client.fixture.FixtureResponse;
import za.ac.vzap.trytons.frontend.client.fixture.FixtureRestClient;
import java.io.IOException;
import java.util.List;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Locale;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.TreeMap;
import java.util.Map;
import java.util.Optional;
import za.ac.vzap.trytons.frontend.client.round.RoundRestClient;
import za.ac.vzap.trytons.frontend.client.fantasyteam.FantasyTeamRestClient;
import za.ac.vzap.trytons.frontend.servlet.shared.AbstractServlet;

import za.ac.vzap.trytons.frontend.client.results.AdminMatchResultRestClient;
import za.ac.vzap.trytons.frontend.client.results.MatchResultResponse;
import za.ac.vzap.trytons.frontend.client.results.MatchTeamScoreResponse;
import za.ac.vzap.trytons.frontend.client.results.MatchTeamScoreRestClient;
import za.ac.vzap.trytons.frontend.client.results.PlayerStatisticsResponse;
import za.ac.vzap.trytons.frontend.client.scoring.FantasyPointBreakdownResponse;
import za.ac.vzap.trytons.frontend.client.scoring.FantasyPointsResponse;
import za.ac.vzap.trytons.frontend.client.scoring.FantasyPointsRestClient;

@WebServlet(name ="FixtureServlet", urlPatterns = {"/fixtures","/fixture"} )
public class FixtureServlet extends AbstractServlet {
    @Inject
    private FixtureRestClient fixtureRestClient;
    @Inject
    private AdminMatchResultRestClient matchResultRestClient;
    @Inject
    private MatchTeamScoreRestClient matchTeamScoreRestClient;
    @Inject
    private FantasyPointsRestClient fantasyPointsRestClient;
    @Inject
    private RoundRestClient roundRestClient;
    @Inject
    private FantasyTeamRestClient fantasyTeamRestClient;

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String submit = request.getParameter("submit");
        if (submit == null) {
            submit = "";
        }
        switch (submit) {
            case "fixture" -> {
                String fixtureId = request.getParameter("fixtureId");
                if (fixtureId == null || fixtureId.isBlank()) {
                    forwardWithError(request, response, "Invalid or missing fixtureId", "/pages/fixtures.jsp");
                    return;
                }
                Optional<FixtureResponse> fixture = fixtureRestClient.getFixture(fixtureId);
                if (fixture.isPresent()) {
                    request.setAttribute("fixture", fixture.get());
                    loadMatchResultReadBack(request, fixtureId);
                    loadBreakdownDrillDown(request);
                    request.getRequestDispatcher("/pages/fixture-details.jsp").forward(request, response);
                    return;
                }
                forwardWithError(request, response, "Fixture not found", "/pages/fixtures.jsp");
            }

            default -> {
                String statusFilter = request.getParameter("status");
                Optional<List<FixtureResponse>> fixtures = fixtureRestClient.listFixtures(statusFilter);
                request.setAttribute("statusFilter", statusFilter);
                if (fixtures.isPresent()) {
                    request.setAttribute("fixtures", fixtures.get());
                    decorateFixtureList(request, fixtures.get());
                    request.getRequestDispatcher("/pages/fixtures.jsp").forward(request, response);
                } else {
                    request.setAttribute("fixtures", List.of());
                    forwardWithError(request, response, "Unable to load fixtures", "/pages/fixtures.jsp");
                }
            }
        }
    }

    /**
     * Supplies what the fixtures list needs beyond the raw DTOs.
     *
     * FixtureResponse carries only a roundId and no score, so:
     *  - round numbers are resolved from the rounds list, letting the page group by
     *    "Round n" rather than falling back to grouping by date;
     *  - scores are fetched per COMPLETED fixture. The list endpoint does not include
     *    them and there is no bulk results call, so this is one request per completed
     *    fixture — fine at this scale, worth revisiting if a season's worth is listed
     *    at once;
     *  - the caller's own team id lets the page highlight their name in a matchup.
     */
    private void decorateFixtureList(HttpServletRequest request, List<FixtureResponse> fixtures) {
        Map<String, Integer> roundNumbers = new HashMap<>();
        roundRestClient.listRounds().orElse(List.of()).forEach(
                round -> roundNumbers.put(String.valueOf(round.getRoundId()), round.getRoundNumber()));
        request.setAttribute("roundNumbersById", roundNumbers);

        Map<String, MatchResultResponse> scores = new HashMap<>();
        for (FixtureResponse fixture : fixtures) {
            if (fixture == null || fixture.getFixtureId() == null) continue;
            if (!"COMPLETED".equalsIgnoreCase(fixture.getFixtureStatus())) continue;
            matchResultRestClient.getMatchResult(fixture.getFixtureId().toString())
                    .ifPresent(result -> scores.put(fixture.getFixtureId().toString(), result));
        }
        request.setAttribute("scoresByFixtureId", scores);

        request.setAttribute("myTeamId", fantasyTeamRestClient.getMyTeam()
                .map(team -> team.getTeamId() == null ? null : team.getTeamId().toString())
                .orElse(null));

        request.setAttribute("fixtureGroups", groupByRound(fixtures, roundNumbers));
        request.setAttribute("featuredFixture", pickFeatured(fixtures));

        // Dates and times are formatted here rather than in the JSP: fixtureDate is a
        // LocalDate and fixtureTime a LocalTime, and fmt:formatDate takes java.util.Date.
        Map<String, String> dateLabels = new HashMap<>();
        Map<String, String> timeLabels = new HashMap<>();
        for (FixtureResponse fixture : fixtures) {
            if (fixture == null || fixture.getFixtureId() == null) continue;
            String key = fixture.getFixtureId().toString();
            if (fixture.getFixtureDate() != null) {
                dateLabels.put(key, fixture.getFixtureDate().format(FIXTURE_DATE));
            }
            if (fixture.getFixtureTime() != null) {
                timeLabels.put(key, fixture.getFixtureTime().format(FIXTURE_TIME));
            }
        }
        request.setAttribute("fixtureDateById", dateLabels);
        request.setAttribute("fixtureTimeById", timeLabels);
    }

    private static final DateTimeFormatter FIXTURE_DATE =
            DateTimeFormatter.ofPattern("EEE d MMM yyyy", Locale.UK);
    private static final DateTimeFormatter FIXTURE_TIME =
            DateTimeFormatter.ofPattern("HH:mm", Locale.UK);

    /** Fixtures grouped under a "Round n" label, highest round first. */
    private Map<String, List<FixtureResponse>> groupByRound(List<FixtureResponse> fixtures,
                                                            Map<String, Integer> roundNumbers) {
        Map<Integer, List<FixtureResponse>> byRound = new TreeMap<>(Comparator.reverseOrder());
        for (FixtureResponse fixture : fixtures) {
            if (fixture == null) continue;
            // Unknown rounds sort last under their own heading rather than being dropped.
            Integer number = fixture.getRoundId() == null
                    ? null
                    : roundNumbers.get(fixture.getRoundId().toString());
            byRound.computeIfAbsent(number == null ? Integer.MIN_VALUE : number, key -> new ArrayList<>())
                    .add(fixture);
        }

        Map<String, List<FixtureResponse>> labelled = new LinkedHashMap<>();
        byRound.forEach((number, group) ->
                labelled.put(number == Integer.MIN_VALUE ? "Other fixtures" : "Round " + number, group));
        return labelled;
    }

    /**
     * The fixture to feature: the soonest one still to be played. Falls back to the
     * first fixture in the list, which on a fully-played season means the hero shows a
     * completed match — the page labels it accordingly rather than calling it "next".
     */
    private FixtureResponse pickFeatured(List<FixtureResponse> fixtures) {
        return fixtures.stream()
                .filter(fixture -> fixture != null && fixture.getFixtureDate() != null)
                .filter(fixture -> !"COMPLETED".equalsIgnoreCase(fixture.getFixtureStatus())
                        && !"PROCESSED".equalsIgnoreCase(fixture.getFixtureStatus())
                        && !"CANCELLED".equalsIgnoreCase(fixture.getFixtureStatus()))
                .min(Comparator.comparing(FixtureResponse::getFixtureDate))
                .orElseGet(() -> fixtures.isEmpty() ? null : fixtures.get(0));
    }

    // Loads the match result read-back for a completed fixture: the result itself, both teams'
    // match-team-scores, and the player-statistics captured for that result. Silently leaves the
    // request attributes unset if no result exists yet (fixture not simulated) - the JSP treats an
    // absent "matchResult" attribute as "no result available".
    private void loadMatchResultReadBack(HttpServletRequest request, String fixtureId) {
        Optional<MatchResultResponse> matchResult = matchResultRestClient.getMatchResult(fixtureId);
        if (matchResult.isEmpty()) {
            return;
        }
        MatchResultResponse result = matchResult.get();
        request.setAttribute("matchResult", result);

        String resultId = result.getResultId().toString();

        Optional<List<MatchTeamScoreResponse>> teamScores = matchTeamScoreRestClient.listMatchTeamScoresForResult(resultId);
        teamScores.ifPresent(scores -> request.setAttribute("teamScores", scores));

        Optional<List<PlayerStatisticsResponse>> playerStats = matchResultRestClient.listResultStatistics(resultId);
        playerStats.ifPresent(stats -> request.setAttribute("playerStats", stats));
    }

    // Optional drill-down: when the page is reloaded with ?statId=<uuid> (a link next to a row in
    // the player-statistics table), resolves that stat's final fantasy points and the points'
    // breakdown lines, so the JSP can render a breakdown table for the selected player only.
    // Iterating every player's breakdown on every fixture-details load would be an N+1 fan-out over
    // fantasy-points and fantasy-point-breakdowns per player, so it is surfaced on-demand instead.
    private void loadBreakdownDrillDown(HttpServletRequest request) {
        String statId = request.getParameter("statId");
        if (statId == null || statId.isBlank()) {
            return;
        }
        Optional<FantasyPointsResponse> finalPoints = fantasyPointsRestClient.getFinalFantasyPointsForStat(statId);
        if (finalPoints.isEmpty()) {
            return;
        }
        request.setAttribute("selectedPoints", finalPoints.get());

        Optional<List<FantasyPointBreakdownResponse>> breakdowns =
                fantasyPointsRestClient.listBreakdownsForPoints(finalPoints.get().getPointsId().toString());
        breakdowns.ifPresent(list -> request.setAttribute("breakdowns", list));
    }
    }

