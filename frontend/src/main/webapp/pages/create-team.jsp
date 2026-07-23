<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="java.util.List" %>
<%@ page import="java.util.Map" %>
<%@ page import="java.util.UUID" %>
<%@ page import="za.ac.vzap.trytons.frontend.client.catalog.PlayerResponse" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="t" tagdir="/WEB-INF/tags" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>

<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Create Team - Fantasy TryTons</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/theme.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/sidebar.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/catalog.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/create-team.css">
</head>
<body class="catalog-page ct-page">

<c:set var="activeNav" value="create-team" scope="request" />
<%@ include file="/WEB-INF/jspf/sidebar.jspf" %>

<main class="catalog-main">
    <div class="catalog-backdrop" aria-hidden="true"></div>

    <div class="catalog-content">

        <%
            List<PlayerResponse> players = (List<PlayerResponse>) request.getAttribute("players");
            // Fallbacks only, for when the servlet supplies neither. Both must
            // track the backend: budget is millions of rands to match
            // player.value (FantasyTeamServiceImpl.INITIAL_BUDGET) and the squad
            // rule in SquadValidationServiceImpl is 20 players, not 15.
            Object budget = request.getAttribute("budget");
            if (budget == null) {
                budget = "196";
            }
            Object squadSize = request.getAttribute("squadSize");
            if (squadSize == null) {
                squadSize = "20";
            }
            request.setAttribute("budget", budget);
            request.setAttribute("squadSize", squadSize);
        %>

        <header class="catalog-header">
            <div>
                <p class="catalog-eyebrow">Fantasy TryTons League</p>
                <h1 class="brand-font">Create Team</h1>
            </div>
        </header>

        <c:if test="${not empty error}">
            <p class="ct-alert ct-alert-error" role="alert"><c:out value="${error}" /></p>
        </c:if>

        <c:if test="${not empty validationErrors}">
            <div class="ct-alert ct-alert-error" role="alert">
                Please fix the following:
                <ul>
                    <c:forEach var="validationError" items="${validationErrors}">
                        <li><c:out value="${validationError}" /></li>
                    </c:forEach>
                </ul>
            </div>
        </c:if>

        <c:if test="${not empty message}">
            <p class="ct-alert ct-alert-success" role="status"><c:out value="${message}" /></p>
        </c:if>

        <form method="post" action="${pageContext.request.contextPath}/create-team" id="createTeamForm"
              data-budget="${fn:escapeXml(budget)}">

            <%-- Sits above the two columns, not inside the pool, so the table and the
                 squad summary start on the same line. --%>
            <div class="catalog-toolbar">
                <label class="search-wrap" for="playerSearch">
                    <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true"><circle cx="11" cy="11" r="7"/><path d="M21 21l-4.3-4.3"/></svg>
                    <input type="search" id="playerSearch" placeholder="Search players"
                           autocomplete="off" aria-label="Search players">
                </label>
                <p class="ct-budget-note">Your budget: <strong><t:money value="${budget}" /></strong></p>
            </div>

            <div class="ct-layout">

                <%-- ---------- Player pool ---------- --%>
                <section>
                    <% if (players == null) { %>
                        <p class="catalog-empty">The players could not be loaded right now. Please try again later.</p>
                    <% } else if (players.isEmpty()) { %>
                        <p class="catalog-empty">No players are available for selection yet.</p>
                    <% } else { %>
                        <div class="ctable">
                            <div class="crow chead">
                                <span aria-hidden="true"></span>
                                <span>Player</span>
                                <span>Club</span>
                                <span>Position</span>
                                <span>Value</span>
                                <span>Status</span>
                            </div>
                            <div class="cbody">
                                <%
                                    Map<UUID, String> clubNamesById = (Map<UUID, String>) request.getAttribute("clubNamesById");
                                    Map<UUID, String> positionNamesById = (Map<UUID, String>) request.getAttribute("positionNamesById");
                                    for (PlayerResponse p : players) {
                                        if (p == null || p.getPlayerId() == null) {
                                            continue;
                                        }
                                        String clubName = (clubNamesById == null) ? "-" : clubNamesById.getOrDefault(p.getClubId(), "-");
                                        String positionName = (positionNamesById == null) ? "-" : positionNamesById.getOrDefault(p.getPositionId(), "-");
                                        pageContext.setAttribute("player", p);
                                        pageContext.setAttribute("clubName", clubName);
                                        pageContext.setAttribute("positionName", positionName);
                                %>
                                <label class="crow" data-team-row>
                                    <span>
                                        <input class="ct-check"
                                               type="checkbox"
                                               name="playerIds"
                                               value="<%= p.getPlayerId() %>"
                                               data-player-name="${fn:escapeXml(player.playerName)}"
                                               data-value="<%= p.getValue() %>"
                                               <%= p.isActive() ? "" : "disabled" %>>
                                    </span>
                                    <span class="c-name">
                                        <span class="c-name-text" title="${fn:escapeXml(player.playerName)}">${fn:escapeXml(player.playerName)}</span>
                                    </span>
                                    <span class="c-text" title="${fn:escapeXml(clubName)}">${fn:escapeXml(clubName)}</span>
                                    <span class="c-text">${fn:escapeXml(positionName)}</span>
                                    <span class="ct-value"><t:money value="${player.value}" /></span>
                                    <span>
                                        <c:choose>
                                            <c:when test="${player.active}">
                                                <span class="avail avail-ok"><span class="avail-label">Available</span></span>
                                            </c:when>
                                            <c:otherwise>
                                                <span class="avail avail-out"><span class="avail-label">Unavailable</span></span>
                                            </c:otherwise>
                                        </c:choose>
                                    </span>
                                </label>
                                <%
                                    }
                                %>
                            </div>
                        </div>
                    <% } %>
                </section>

                <%-- ---------- Squad summary ---------- --%>
                <aside class="ct-summary">
                    <h2>Your Squad</h2>
                    <p class="ct-summary-sub">Pick ${fn:escapeXml(squadSize)} players within <t:money value="${budget}" /></p>

                    <div class="ct-stat">
                        <span class="ct-stat-label">Players selected</span>
                        <span class="ct-stat-value"><span id="selectedCount">0</span> / ${fn:escapeXml(squadSize)}</span>
                    </div>
                    <div class="ct-stat">
                        <span class="ct-stat-label">Budget</span>
                        <span class="ct-stat-value" id="budgetTotal"><t:money value="${budget}" /></span>
                    </div>
                    <div class="ct-stat">
                        <span class="ct-stat-label">Selected value</span>
                        <span class="ct-stat-value" id="budgetUsed"><t:money value="0" /></span>
                    </div>
                    <div class="ct-stat">
                        <span class="ct-stat-label">Remaining</span>
                        <span class="ct-stat-value is-gold" id="budgetRemaining"><t:money value="${budget}" /></span>
                    </div>

                    <p class="ct-alert ct-alert-error" role="alert" id="overBudgetWarning" hidden>
                        You are over budget. You can still submit, but the server will reject an over-budget squad.
                    </p>

                    <ul class="ct-selected" id="selectedList"></ul>

                    <div class="ct-field">
                        <label class="ct-label" for="teamName">Team name</label>
                        <input class="ct-input" type="text" id="teamName" name="teamName"
                               placeholder="Name your team" required>
                    </div>

                    <button type="submit" name="submit" value="create-team" class="btn-gold ct-submit">Create team</button>

                    <p class="ct-footnote">
                        Budget numbers are a preview only &mdash; final totals and squad rules are
                        checked on the server.
                    </p>
                </aside>

            </div>
        </form>

    </div>
</main>

<script src="${pageContext.request.contextPath}/assets/js/create-team.js"></script>
</body>
</html>
