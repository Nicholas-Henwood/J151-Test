package za.ac.vzap.trytons.frontend.servlet.history;

import jakarta.inject.Inject;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import za.ac.vzap.trytons.frontend.client.history.UserHistoryRestClient;
import za.ac.vzap.trytons.frontend.client.round.RoundRestClient;
import za.ac.vzap.trytons.frontend.client.fixture.FixtureRestClient;
import za.ac.vzap.trytons.frontend.client.history.UserPointsHistoryResponse;
import za.ac.vzap.trytons.frontend.client.history.WeeklyPerformanceResponse;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import za.ac.vzap.trytons.frontend.servlet.shared.AbstractServlet;


@WebServlet(name = "UserHistoryServlet", urlPatterns = {"/history"})
public class UserHistoryServlet extends AbstractServlet {

    @Inject
    private UserHistoryRestClient userHistoryRestClient;

    @Inject
    private RoundRestClient roundRestClient;

    @Inject
    private FixtureRestClient fixtureRestClient;

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        if(!requireAuthenticated(request, response)) return;
        String destination = "/pages/history.jsp";

        Optional<UserPointsHistoryResponse> pointsHistory = userHistoryRestClient.getUserPointsHistory();
        if (pointsHistory.isPresent()) {
            request.setAttribute("pointsHistory", pointsHistory.get());
        } else {
            request.setAttribute("pointsHistoryError", "Unable to load user points history");
        }

        Optional<List<WeeklyPerformanceResponse>> weeklyPerformance = userHistoryRestClient.getWeeklyPerformance();
        if (weeklyPerformance.isPresent()){
            request.setAttribute("weeklyPerformance", weeklyPerformance.get());
        } else {
            request.setAttribute("weeklyPerformanceError", "Unable to load user weekly performance");
        }

        decorateHistory(request, weeklyPerformance.orElseGet(List::of),
                pointsHistory.map(UserPointsHistoryResponse::getTotals).orElse(0));

        request.getRequestDispatcher(destination).forward(request, response);
    }

    /**
     * Supplies what WeeklyPerformanceResponse does not carry.
     *
     * The DTO holds only a roundId and a fixtureId, both UUIDs, so this resolves
     * friendly labels: round numbers from the rounds list and matchup names from the
     * fixtures list — one call each rather than a lookup per row. Rows whose ids do not
     * resolve simply fall back to the raw id in the page.
     *
     * The record, average and best-round figures are derived from the same list. They
     * are computed here rather than in JSTL, which cannot easily max or count.
     */
    private void decorateHistory(HttpServletRequest request,
                                 List<WeeklyPerformanceResponse> rounds,
                                 int totals) {
        // Both maps are keyed by UUID, not by the id's string form: the page looks them
        // up with ${map[round.roundId]}, and WeeklyPerformanceResponse exposes those ids
        // as UUIDs. A String key never matches that lookup, which left every row falling
        // back to the raw id.
        Map<UUID, Integer> roundNumbers = new HashMap<>();
        roundRestClient.listRounds().orElse(List.of()).forEach(
                round -> parseUuid(round.getRoundId())
                        .ifPresent(roundId -> roundNumbers.put(roundId, round.getRoundNumber())));
        request.setAttribute("roundNumbersById", roundNumbers);

        Map<UUID, String> fixtureLabels = new HashMap<>();
        fixtureRestClient.listFixtures(null).orElse(List.of()).forEach(fixture -> {
            if (fixture.getFixtureId() != null) {
                fixtureLabels.put(fixture.getFixtureId(),
                        fixture.getTeamAName() + " vs " + fixture.getTeamBName());
            }
        });
        request.setAttribute("fixtureLabelsById", fixtureLabels);

        int wins = 0, draws = 0, losses = 0, best = 0;
        for (WeeklyPerformanceResponse round : rounds) {
            if (round == null) continue;
            String result = round.getResult() == null ? "" : round.getResult().toUpperCase(Locale.ROOT);
            if (result.equals("WIN")) wins++;
            else if (result.equals("DRAW")) draws++;
            else if (result.equals("LOSS")) losses++;
            best = Math.max(best, round.getPointsScored());
        }

        request.setAttribute("wins", wins);
        request.setAttribute("draws", draws);
        request.setAttribute("losses", losses);
        request.setAttribute("bestRound", best);
        request.setAttribute("averagePerRound", rounds.isEmpty() ? 0 : Math.round((float) totals / rounds.size()));
    }
}