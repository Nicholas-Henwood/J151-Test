package za.ac.vzap.trytons.frontend.servlet.league;

import jakarta.inject.Inject;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import za.ac.vzap.trytons.frontend.client.league.JoinLeagueRequest;
import za.ac.vzap.trytons.frontend.client.league.JoinLeagueResponse;
import za.ac.vzap.trytons.frontend.client.league.LeagueMemberResponse;
import za.ac.vzap.trytons.frontend.client.league.LeagueRequest;
import za.ac.vzap.trytons.frontend.client.league.LeagueResponse;
import za.ac.vzap.trytons.frontend.client.league.LeagueRestClient;
import za.ac.vzap.trytons.frontend.client.leaderboard.LeaderboardEntryResponse;
import za.ac.vzap.trytons.frontend.client.leaderboard.LeaderboardRestClient;
import za.ac.vzap.trytons.frontend.client.fantasyteam.FantasyTeamRestClient;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import za.ac.vzap.trytons.frontend.servlet.shared.AbstractServlet;

@WebServlet(name = "LeagueServlet", urlPatterns = {"/leagues", "/league", "/league/create", "/league/join", "/league/members"})
public class LeagueServlet extends AbstractServlet {

    @Inject
    private LeagueRestClient leagueRestClient;

    @Inject
    private LeaderboardRestClient leaderboardRestClient;

    @Inject
    private FantasyTeamRestClient fantasyTeamRestClient;

    /**
     * A league membership requires a team (leagueMembership.teamId is NOT NULL),
     * so joining is impossible until the user has created one. Both league pages
     * use this to explain that up front rather than letting a join fail.
     */
    private boolean currentUserHasTeam() {
        return authContext.isAuthenticated() && fantasyTeamRestClient.getMyTeam().isPresent();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String path = request.getServletPath();

        if ("/league/members".equals(path) && !requireAuthenticated(request, response)) return;

        String destination = switch (path) {
            case "/leagues" -> {
                Optional<List<LeagueResponse>> publicLeagues = leagueRestClient.listPublicLeagues();
                request.setAttribute("publicLeagues", publicLeagues.orElseGet(List::of));
                if (publicLeagues.isEmpty()) {
                    request.setAttribute("error", "Unable to load leagues right now");
                }

                request.setAttribute("myLeagues", loadMyLeagues());
                request.setAttribute("hasTeam", currentUserHasTeam());
                populateLeaguesView(request, publicLeagues.orElseGet(List::of));
                yield "/pages/leagues.jsp";
            }

            case "/league" -> {
                String leagueId = request.getParameter("leagueId");
                if (leagueId == null || leagueId.isBlank()) {
                    request.setAttribute("error", "A league id is required");
                    yield "/pages/leagues.jsp";
                }
                leagueRestClient.getLeague(leagueId).ifPresentOrElse(
                        league -> request.setAttribute("league", league),
                        () -> request.setAttribute("error", "League not found, or you don't have access to view it")
                );
                yield "/pages/leagues.jsp";
            }

            case "/league/create" -> {
                // Creating a league enrols the creator as its first member, which needs a
                // team. The button on the leagues page is disabled without one, but this
                // URL is reachable directly, so the form is withheld here too rather than
                // letting a filled-in form fail on submit.
                request.setAttribute("hasTeam", currentUserHasTeam());
                yield "/pages/create-league.jsp";
            }

            case "/league/join" -> {
                // The page browses public leagues by name rather than asking for an id,
                // so it needs the same list and member counts the leagues hub uses.
                Optional<List<LeagueResponse>> allVisible = leagueRestClient.listPublicLeagues();
                if (allVisible.isEmpty()) {
                    // GET /league is @Authenticated, so an expired session returns nothing.
                    // Without this the page just showed "0 leagues" and looked broken.
                    request.setAttribute("error", authContext.isAuthenticated()
                            ? "Unable to load leagues right now. Please try again."
                            : "Please sign in to browse and join leagues.");
                }
                // The endpoint returns every public league PLUS any private league the
                // caller belongs to, so filter: a private league must not be listed here
                // as joinable, and must never be labelled Public.
                List<LeagueResponse> joinable = allVisible.orElseGet(List::of).stream()
                        .filter(league -> "PUBLIC".equalsIgnoreCase(league.getLeagueType()))
                        .collect(Collectors.toList());
                request.setAttribute("hasTeam", currentUserHasTeam());
                request.setAttribute("publicLeagues", joinable);
                request.setAttribute("memberCounts", countMembers(joinable));
                yield "/pages/join-league.jsp";
            }

            case "/league/members" -> {
                String leagueId = request.getParameter("leagueId");
                request.setAttribute("leagueId", leagueId);
                List<LeagueMemberResponse> members = reloadMembers(leagueId);
                request.setAttribute("members", members);
                request.setAttribute("isLeagueManager", isCurrentUserManager(leagueId));
                populateMembersView(request, leagueId, members);
                yield "/pages/league-members.jsp";
            }

            default -> "/index.jsp";
        };

        request.getRequestDispatcher(destination).forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        if (!requireAuthenticated(request, response)) return;

        String submit = request.getParameter("submit");
        if (submit == null) submit = "";

        switch (submit) {
            case "league/create" -> {
                Optional<LeagueResponse> created = leagueRestClient.createLeague(buildLeagueRequest(request));
                if (created.isPresent()) {
                    response.sendRedirect(request.getContextPath() + "/league?leagueId=" + created.get().getLeagueId());
                    return;
                }
                if (handleApiFailure(request, response, "Unable to create league. Check your details and try again.")) return;
                request.getRequestDispatcher("/pages/create-league.jsp").forward(request, response);
            }

            case "league/join" -> {
                JoinLeagueRequest joinRequest = new JoinLeagueRequest(
                        request.getParameter("leagueId"),
                        request.getParameter("leagueCode"));
                Optional<JoinLeagueResponse> joined = leagueRestClient.joinLeague(joinRequest);
                if (joined.isPresent()) {
                    response.sendRedirect(request.getContextPath() + "/league?leagueId=" + joined.get().getLeagueId());
                    return;
                }
                if (handleApiFailure(request, response, "Unable to join that league. Check the join code and try again.")) return;
                request.setAttribute("hasTeam", currentUserHasTeam());
                request.getRequestDispatcher("/pages/join-league.jsp").forward(request, response);
            }

            case "league/members/remove" -> {
                String leagueId = request.getParameter("leagueId");
                String membershipId = request.getParameter("membershipId");
                boolean removed = false;
                if (leagueId != null && membershipId != null) {
                    leagueRestClient.removeMember(leagueId, membershipId);
                    removed = apiCallStatus.isSuccess();
                }
                if (removed) {
                    response.sendRedirect(request.getContextPath() + "/league/members?leagueId=" + leagueId);
                    return;
                }
                if (handleApiFailure(request, response, "Unable to remove member")) return;
                request.setAttribute("leagueId", leagueId);
                List<LeagueMemberResponse> members = reloadMembers(leagueId);
                request.setAttribute("members", members);
                request.setAttribute("isLeagueManager", isCurrentUserManager(leagueId));
                populateMembersView(request, leagueId, members);
                request.getRequestDispatcher("/pages/league-members.jsp").forward(request, response);
            }

            default -> request.getRequestDispatcher("/index.jsp").forward(request, response);
        }
    }

    private LeagueRequest buildLeagueRequest(HttpServletRequest request) {
        return new LeagueRequest(
                request.getParameter("leagueName"),
                request.getParameter("description"),
                request.getParameter("leagueType"),
                parseIntOrZero(request.getParameter("maxMembers")));
    }

    private List<LeagueMemberResponse> reloadMembers(String leagueId) {
        if (leagueId == null || leagueId.isBlank()) return List.of();
        return leagueRestClient.listMembers(leagueId).orElse(List.of());
    }

    /**
     * Header data for the members page: the league itself (name, type, invite code,
     * capacity, creation date, manager) which the members list does not carry, plus
     * the active-member count / spots-left and the date labels.
     *
     * joinDate and creationDate are LocalDateTime, and fmt:formatDate takes a
     * java.util.Date, so the display strings are built here.
     */
    private void populateMembersView(HttpServletRequest request, String leagueId,
                                     List<LeagueMemberResponse> members) {
        if (leagueId == null || leagueId.isBlank()) return;

        leagueRestClient.getLeague(leagueId).ifPresent(league -> {
            request.setAttribute("league", league);
            if (league.getCreationDate() != null) {
                request.setAttribute("creationDateLabel", league.getCreationDate().format(MEMBER_DATE));
            }
            // member.userId is a String and managerUserId a UUID; expose the string form
            // so the "Manager" badge can compare them in EL.
            if (league.getManagerUserId() != null) {
                request.setAttribute("managerUserId", league.getManagerUserId().toString());
            }
        });

        long activeCount = members.stream().filter(LeagueMemberResponse::isActive).count();
        request.setAttribute("memberCount", (int) activeCount);

        Map<String, String> joinLabels = new HashMap<>();
        for (LeagueMemberResponse member : members) {
            if (member != null && member.getMembershipId() != null && member.getJoinDate() != null) {
                joinLabels.put(member.getMembershipId(), member.getJoinDate().format(MEMBER_DATE));
            }
        }
        request.setAttribute("joinDateLabels", joinLabels);
    }

    private static final java.time.format.DateTimeFormatter MEMBER_DATE =
            java.time.format.DateTimeFormatter.ofPattern("d MMM yyyy", java.util.Locale.UK);

    private List<LeagueResponse> loadMyLeagues() {
        if (!authContext.isAuthenticated()) return List.of();
        UUID currentUserId = authContext.getUserId();
        if (currentUserId == null) return List.of();
        return leagueRestClient.listMyLeagues().orElse(List.of()).stream()
                .filter(league -> currentUserId.equals(league.getManagerUserId()))
                .collect(Collectors.toList());
    }

    /**
     * Supplies the extra data the leagues page needs beyond the raw league lists:
     * the master leaderboard behind the spotlight, per-league member counts, the
     * standings behind each mini leaderboard, and the split between leagues the
     * user belongs to and ones they could still join.
     *
     * Membership is derived from the member lists rather than {@link #loadMyLeagues()},
     * which can only detect leagues the user *manages* — LeagueResponseDTO carries no
     * membership flag. The member list is fetched anyway for the counts, so reusing it
     * costs nothing and makes "Your leagues" reflect joined leagues too.
     *
     * One listMembers call per league. Fine at this scale; worth revisiting if the
     * league count grows.
     */
    private void populateLeaguesView(HttpServletRequest request, List<LeagueResponse> publicLeagues) {
        List<LeagueResponse> candidates = authContext.isAuthenticated()
                ? leagueRestClient.listMyLeagues().orElse(publicLeagues)
                : publicLeagues;

        UUID currentUserId = authContext.isAuthenticated() ? authContext.getUserId() : null;
        String currentUsername = authContext.getUsername();

        Map<String, Integer> memberCounts = new HashMap<>();
        Map<String, List<LeaderboardEntryResponse>> leagueStandings = new HashMap<>();
        List<LeagueResponse> memberLeagues = new ArrayList<>();
        List<LeagueResponse> discoverLeagues = new ArrayList<>();

        for (LeagueResponse league : candidates) {
            String leagueId = league.getLeagueId();
            if (leagueId == null || leagueId.isBlank()) continue;

            List<LeagueMemberResponse> active = activeMembers(leagueId);
            memberCounts.put(leagueId, active.size());

            boolean isMember = currentUserId != null && active.stream()
                    .anyMatch(member -> currentUserId.toString().equals(member.getUserId()));
            boolean isManager = currentUserId != null && currentUserId.equals(league.getManagerUserId());

            if (isMember || isManager) {
                memberLeagues.add(league);
                parseUuid(leagueId).ifPresent(id -> leaderboardRestClient.getLeaderboardForLeague(id)
                        .ifPresent(standings -> leagueStandings.put(leagueId, standings)));
            } else if ("PUBLIC".equalsIgnoreCase(league.getLeagueType())) {
                discoverLeagues.add(league);
            }
        }

        request.setAttribute("memberLeagues", memberLeagues);
        request.setAttribute("discoverLeagues", discoverLeagues);
        request.setAttribute("memberCounts", memberCounts);
        request.setAttribute("leagueStandings", leagueStandings);
        request.setAttribute("currentUsername", currentUsername);
        request.setAttribute("masterStandings", leaderboardRestClient.getOverallLeaderboard().orElse(List.of()));
    }

    private List<LeagueMemberResponse> activeMembers(String leagueId) {
        return leagueRestClient.listMembers(leagueId).orElse(List.of()).stream()
                .filter(LeagueMemberResponse::isActive)
                .collect(Collectors.toList());
    }

    private Map<String, Integer> countMembers(List<LeagueResponse> leagues) {
        Map<String, Integer> counts = new HashMap<>();
        for (LeagueResponse league : leagues) {
            String leagueId = league.getLeagueId();
            if (leagueId != null && !leagueId.isBlank()) {
                counts.put(leagueId, activeMembers(leagueId).size());
            }
        }
        return counts;
    }

    private boolean isCurrentUserManager(String leagueId) {
        if (leagueId == null || leagueId.isBlank() || !authContext.isAuthenticated()) return false;
        UUID currentUserId = authContext.getUserId();
        if (currentUserId == null) return false;
        return leagueRestClient.getLeague(leagueId)
                .map(LeagueResponse::getManagerUserId)
                .map(currentUserId::equals)
                .orElse(false);
    }

    private int parseIntOrZero(String value) {
        try {
            return Integer.parseInt(value);
        } catch (Exception e) {
            return 0;
        }
    }

    @Override
    public String getServletInfo() {
        return "League Servlet, handles league browsing, detail, create, join, and member management";
    }
}
