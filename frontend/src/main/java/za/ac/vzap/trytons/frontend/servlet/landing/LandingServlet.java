package za.ac.vzap.trytons.frontend.servlet.landing;

import jakarta.inject.Inject;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import za.ac.vzap.trytons.frontend.client.leaderboard.LeaderboardEntryResponse;
import za.ac.vzap.trytons.frontend.client.publicpreview.PublicLeagueResponse;
import za.ac.vzap.trytons.frontend.client.publicpreview.PublicPreviewRestClient;
import za.ac.vzap.trytons.frontend.servlet.shared.AbstractServlet;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Public, pre-auth landing page. This is the application entry point ("/" forwards
 * here via index.jsp). Already-authenticated visitors are sent straight to their
 * dashboard. Previews are live but read-only, pulled from the backend's /public
 * endpoints which need no bearer token.
 */
@WebServlet(name = "LandingServlet", urlPatterns = {"/landing"})
public class LandingServlet extends AbstractServlet {

    private static final String LANDING_JSP = "/pages/landing.jsp";
    private static final int PREVIEW_LIMIT = 6;

    // Season label and a season's round count are not exposed by any public endpoint,
    // so they are presentation constants (kept in step with the rest of the app).
    private static final String SEASON_LABEL = "2025/26";
    private static final int ROUNDS_PER_SEASON = 26;

    @Inject
    private PublicPreviewRestClient publicPreviewRestClient;

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        // Logged-in users have no use for a sign-up page.
        if (authContext != null && authContext.isAuthenticated()) {
            response.sendRedirect(request.getContextPath() + "/dashboard");
            return;
        }

        List<PublicLeagueResponse> allLeagues = publicPreviewRestClient.listPublicLeagues();
        List<LeaderboardEntryResponse> allEntries = publicPreviewRestClient.getPublicLeaderboard(0);

        // The master leaderboard is not returned in rank order, so sort before we
        // slice the top N — otherwise the "top of the table" podium is wrong.
        List<LeaderboardEntryResponse> rankedEntries = new ArrayList<>(allEntries);
        rankedEntries.sort(Comparator.comparingInt(LeaderboardEntryResponse::getRank));

        // "Popular" leagues: fullest first.
        List<PublicLeagueResponse> sortedLeagues = new ArrayList<>(allLeagues);
        sortedLeagues.sort(Comparator.comparingInt(PublicLeagueResponse::getMemberCount).reversed());

        request.setAttribute("publicLeagues", firstN(sortedLeagues, PREVIEW_LIMIT));
        request.setAttribute("publicLeaguesTotal", allLeagues.size());
        request.setAttribute("leaderboard", firstN(rankedEntries, PREVIEW_LIMIT));
        // Every ranked team is one manager: a truthful "active managers" figure.
        request.setAttribute("managersCount", rankedEntries.size());
        request.setAttribute("roundsPerSeason", ROUNDS_PER_SEASON);
        request.setAttribute("season", SEASON_LABEL);

        request.getRequestDispatcher(LANDING_JSP).forward(request, response);
    }

    private static <T> List<T> firstN(List<T> items, int n) {
        if (items == null) {
            return List.of();
        }
        return items.size() > n ? items.subList(0, n) : items;
    }
}
