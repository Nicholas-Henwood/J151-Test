<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>

<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Leagues - Fantasy TryTons</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/theme.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/sidebar.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/catalog.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/leagues.css">
</head>
<body class="catalog-page leagues-page">

<c:set var="activeNav" value="leagues" scope="request" />
<%@ include file="/WEB-INF/jspf/sidebar.jspf" %>

<main class="catalog-main">
    <div class="catalog-content">

        <c:choose>
            <%-- ============ Single league detail (GET /league?leagueId=...) ============ --%>
            <c:when test="${league != null}">
                <header class="catalog-header">
                    <div>
                        <p class="catalog-eyebrow">Fantasy TryTons League</p>
                        <h1 class="brand-font"><c:out value="${league.leagueName}" /></h1>
                    </div>
                    <div class="lg-actions">
                        <a class="btn-outline" href="${pageContext.request.contextPath}/leagues">Back to leagues</a>
                    </div>
                </header>

                <c:if test="${not empty error}">
                    <p class="catalog-error" role="alert"><c:out value="${error}" /></p>
                </c:if>

                <div class="lg-grid" style="margin-top:26px">
                    <section class="lg-card">
                        <div class="lg-card-head">
                            <span class="lg-card-icon">
                                <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M8 21h8"/><path d="M12 17v4"/><path d="M7 4h10v5a5 5 0 0 1-10 0z"/><path d="M17 5h3v2a3 3 0 0 1-3 3"/><path d="M7 5H4v2a3 3 0 0 0 3 3"/></svg>
                            </span>
                            <div class="lg-card-heading">
                                <p class="lg-card-name"><c:out value="${league.leagueName}" /></p>
                                <p class="lg-card-sub">Managed by <c:out value="${empty league.managerDisplayName ? 'the league' : league.managerDisplayName}" /></p>
                            </div>
                            <span class="lg-type ${league.leagueType == 'PRIVATE' ? 'lg-type-private' : 'lg-type-public'}">
                                ${league.leagueType == 'PRIVATE' ? 'Private' : 'Public'}
                            </span>
                        </div>

                        <p class="lg-desc"><c:out value="${league.description}" /></p>

                        <div class="lg-position">
                            <span>
                                <span class="lg-stat-label">Capacity</span>
                                <span class="lg-position-rank">${league.maxMembers}<span class="lg-position-of">teams</span></span>
                            </span>
                            <c:if test="${not empty league.leagueCode}">
                                <span>
                                    <span class="lg-stat-label">League code</span>
                                    <span class="lg-standing-points"><c:out value="${league.leagueCode}" /></span>
                                </span>
                            </c:if>
                        </div>

                        <div class="lg-card-footer">
                            <a class="lg-link" href="${pageContext.request.contextPath}/league/members?leagueId=${league.leagueId}">View members &rarr;</a>
                        </div>
                    </section>
                </div>
            </c:when>

            <%-- ==================== Leagues hub (GET /leagues) ==================== --%>
            <c:otherwise>
                <header class="catalog-header">
                    <div>
                        <p class="catalog-eyebrow">Fantasy TryTons League</p>
                        <h1 class="brand-font">Leagues</h1>
                    </div>
                    <div class="lg-actions">
                        <a class="btn-outline" href="${pageContext.request.contextPath}/league/join">Join league</a>
                        <%-- Creating a league enrols you as its first member and manager, which
                             needs a team just as joining does, so it is gated the same way. --%>
                        <c:choose>
                            <c:when test="${hasTeam}">
                                <a class="btn-gold" href="${pageContext.request.contextPath}/league/create">
                                    <svg width="15" height="15" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round" aria-hidden="true"><path d="M12 5v14"/><path d="M5 12h14"/></svg>
                                    Create league
                                </a>
                            </c:when>
                            <c:otherwise>
                                <button type="button" class="btn-gold" disabled
                                        title="Create a team before creating a league">
                                    <svg width="15" height="15" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round" aria-hidden="true"><path d="M12 5v14"/><path d="M5 12h14"/></svg>
                                    Create league
                                </button>
                            </c:otherwise>
                        </c:choose>
                    </div>
                </header>

                <c:if test="${not empty error}">
                    <p class="catalog-error" role="alert"><c:out value="${error}" /></p>
                </c:if>
                <c:if test="${not empty success}">
                    <p class="catalog-error" role="status" style="border-left-color:#5fae7a"><c:out value="${success}" /></p>
                </c:if>

                <%-- ---------- Spotlight: master leaderboard ---------- --%>
                <c:if test="${not empty masterStandings}">
                    <%-- Locate the signed-in user's row so the rank stats can be personalised.
                         Entries carry an owner username, which is what the session exposes. --%>
                    <c:set var="yourEntry" value="${null}" />
                    <c:forEach var="entry" items="${masterStandings}">
                        <c:if test="${not empty currentUsername and entry.owner == currentUsername}">
                            <c:set var="yourEntry" value="${entry}" />
                        </c:if>
                    </c:forEach>
                    <c:set var="leader" value="${masterStandings[0]}" />

                    <section class="lg-spotlight">
                        <div class="lg-spotlight-inner">
                            <div class="lg-spotlight-content">
                                <span class="lg-pill">Master Leaderboard</span>
                                <h2 class="brand-font">Overall Standings</h2>
                                <p class="lg-spotlight-caption">${fn:length(masterStandings)} teams &middot; Season 2025/26</p>

                                <div class="lg-stats">
                                    <div>
                                        <span class="lg-stat-label">Your rank</span>
                                        <span class="lg-stat-value is-gold">
                                            <c:choose>
                                                <c:when test="${yourEntry != null}">
                                                    ${yourEntry.rank}
                                                    <c:set var="movement" value="${empty yourEntry.rankMovement ? 0 : yourEntry.rankMovement}" />
                                                    <c:choose>
                                                        <c:when test="${movement > 0}"><span class="lg-chip lg-chip-up">&uarr; ${movement}</span></c:when>
                                                        <c:when test="${movement < 0}"><span class="lg-chip lg-chip-down">&darr; ${-movement}</span></c:when>
                                                        <c:otherwise><span class="lg-chip">&ndash;</span></c:otherwise>
                                                    </c:choose>
                                                </c:when>
                                                <c:otherwise>&ndash;</c:otherwise>
                                            </c:choose>
                                        </span>
                                    </div>
                                    <div>
                                        <span class="lg-stat-label">Fantasy points</span>
                                        <span class="lg-stat-value">${yourEntry != null ? yourEntry.totalFantasyPoints : '—'}</span>
                                    </div>
                                    <div>
                                        <span class="lg-stat-label">Gap to top</span>
                                        <span class="lg-stat-value">${yourEntry != null ? leader.totalFantasyPoints - yourEntry.totalFantasyPoints : '—'}</span>
                                    </div>
                                </div>
                            </div>

                            <%-- Podium runs 2nd / 1st / 3rd so first place sits in the middle. --%>
                            <div class="lg-podium">
                                <c:forEach var="slot" items="${[1, 0, 2]}">
                                    <c:if test="${fn:length(masterStandings) > slot}">
                                        <c:set var="podiumEntry" value="${masterStandings[slot]}" />
                                        <div class="lg-podium-col">
                                            <c:if test="${slot == 0}">
                                                <svg class="lg-crown" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true"><path d="M3 7l4.5 4L12 4l4.5 7L21 7l-2 12H5z"/></svg>
                                            </c:if>
                                            <span class="lg-medal lg-medal-${podiumEntry.rank}">${podiumEntry.rank}</span>
                                            <span class="lg-podium-team" title="${fn:escapeXml(podiumEntry.teamName)}">${fn:escapeXml(podiumEntry.teamName)}</span>
                                            <span class="lg-podium-points">${podiumEntry.totalFantasyPoints} pts</span>
                                            <span class="lg-podium-stand"></span>
                                        </div>
                                    </c:if>
                                </c:forEach>
                            </div>
                        </div>
                    </section>
                </c:if>

                <%-- The master leaderboard above still renders for everyone; only the
                     join actions below are gated on having a team. --%>
                <%@ include file="/WEB-INF/jspf/no-team-notice.jspf" %>

                <%-- ---------- Tabs ---------- --%>
                <div class="lg-tabs" role="tablist">
                    <button type="button" class="lg-tab is-active" data-lg-tab="mine" role="tab" aria-selected="true">
                        Your leagues<span class="lg-tab-count">${fn:length(memberLeagues)}</span>
                    </button>
                    <button type="button" class="lg-tab" data-lg-tab="discover" role="tab" aria-selected="false">
                        Discover<span class="lg-tab-count">${fn:length(discoverLeagues)}</span>
                    </button>
                </div>

                <%-- ---------- Your leagues ---------- --%>
                <div data-lg-panel="mine">
                    <c:choose>
                        <c:when test="${empty memberLeagues}">
                            <p class="catalog-empty">You haven't joined a league yet. Try the Discover tab.</p>
                        </c:when>
                        <c:otherwise>
                            <div class="lg-grid">
                                <c:forEach var="myLeague" items="${memberLeagues}">
                                    <c:set var="memberCount" value="${empty memberCounts[myLeague.leagueId] ? 0 : memberCounts[myLeague.leagueId]}" />
                                    <c:set var="standings" value="${leagueStandings[myLeague.leagueId]}" />

                                    <%-- Index of the user's own row, so the mini table can be
                                         centred on it and that row highlighted. --%>
                                    <c:set var="yourIndex" value="${-1}" />
                                    <c:set var="yourRow" value="${null}" />
                                    <c:forEach var="entry" items="${standings}" varStatus="st">
                                        <c:if test="${not empty currentUsername and entry.owner == currentUsername}">
                                            <c:set var="yourIndex" value="${st.index}" />
                                            <c:set var="yourRow" value="${entry}" />
                                        </c:if>
                                    </c:forEach>

                                    <article class="lg-card">
                                        <div class="lg-card-head">
                                            <span class="lg-card-icon">
                                                <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M8 21h8"/><path d="M12 17v4"/><path d="M7 4h10v5a5 5 0 0 1-10 0z"/><path d="M17 5h3v2a3 3 0 0 1-3 3"/><path d="M7 5H4v2a3 3 0 0 0 3 3"/></svg>
                                            </span>
                                            <div class="lg-card-heading">
                                                <%-- Opens the league detail, from which View members / standings branch. --%>
                                                <a class="lg-card-name lg-card-name-link" href="${pageContext.request.contextPath}/league?leagueId=${myLeague.leagueId}" title="${fn:escapeXml(myLeague.leagueName)}">${fn:escapeXml(myLeague.leagueName)}</a>
                                                <p class="lg-card-sub">${memberCount} / ${myLeague.maxMembers} teams</p>
                                            </div>
                                            <span class="lg-type ${myLeague.leagueType == 'PRIVATE' ? 'lg-type-private' : 'lg-type-public'}">
                                                ${myLeague.leagueType == 'PRIVATE' ? 'Private' : 'Public'}
                                            </span>
                                        </div>

                                        <div class="lg-position">
                                            <span>
                                                <span class="lg-stat-label">Your position</span>
                                                <span class="lg-position-rank">
                                                    ${yourRow != null ? yourRow.rank : '—'}
                                                    <span class="lg-position-of">of ${memberCount}</span>
                                                </span>
                                            </span>
                                            <c:if test="${yourRow != null}">
                                                <c:set var="movement" value="${empty yourRow.rankMovement ? 0 : yourRow.rankMovement}" />
                                                <c:choose>
                                                    <c:when test="${movement > 0}"><span class="lg-chip lg-chip-up">&uarr; ${movement}</span></c:when>
                                                    <c:when test="${movement < 0}"><span class="lg-chip lg-chip-down">&darr; ${-movement}</span></c:when>
                                                    <c:otherwise><span class="lg-chip">&ndash;</span></c:otherwise>
                                                </c:choose>
                                            </c:if>
                                        </div>

                                        <c:if test="${not empty standings}">
                                            <%-- Three rows centred on the user, clamped to the ends
                                                 of the table so short leaderboards still fill up. --%>
                                            <c:set var="total" value="${fn:length(standings)}" />
                                            <c:set var="start" value="${yourIndex <= 0 ? 0 : yourIndex - 1}" />
                                            <c:if test="${start > total - 3}"><c:set var="start" value="${total - 3}" /></c:if>
                                            <c:if test="${start < 0}"><c:set var="start" value="${0}" /></c:if>

                                            <div class="lg-standings">
                                                <c:forEach var="entry" items="${standings}" begin="${start}" end="${start + 2}">
                                                    <div class="lg-standing ${not empty currentUsername and entry.owner == currentUsername ? 'is-you' : ''}">
                                                        <span class="lg-standing-rank">${entry.rank}</span>
                                                        <span class="lg-standing-team">
                                                            ${fn:escapeXml(entry.teamName)}
                                                            <span class="lg-standing-owner">@${fn:escapeXml(entry.owner)}</span>
                                                        </span>
                                                        <span class="lg-standing-points">${entry.totalFantasyPoints}</span>
                                                    </div>
                                                </c:forEach>
                                            </div>
                                        </c:if>

                                        <div class="lg-card-footer">
                                            <a class="lg-link" href="${pageContext.request.contextPath}/leaderboard?leagueId=${myLeague.leagueId}">View standings &rarr;</a>
                                        </div>
                                    </article>
                                </c:forEach>
                            </div>
                        </c:otherwise>
                    </c:choose>
                </div>

                <%-- ---------- Discover ---------- --%>
                <div data-lg-panel="discover" hidden>
                    <c:choose>
                        <c:when test="${empty discoverLeagues}">
                            <p class="catalog-empty">There are no other public leagues to join right now.</p>
                        </c:when>
                        <c:otherwise>
                            <div class="lg-grid">
                                <c:forEach var="openLeague" items="${discoverLeagues}">
                                    <c:set var="memberCount" value="${empty memberCounts[openLeague.leagueId] ? 0 : memberCounts[openLeague.leagueId]}" />
                                    <c:set var="spotsLeft" value="${openLeague.maxMembers - memberCount}" />

                                    <article class="lg-card">
                                        <div class="lg-card-head">
                                            <span class="lg-card-icon">
                                                <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M8 21h8"/><path d="M12 17v4"/><path d="M7 4h10v5a5 5 0 0 1-10 0z"/><path d="M17 5h3v2a3 3 0 0 1-3 3"/><path d="M7 5H4v2a3 3 0 0 0 3 3"/></svg>
                                            </span>
                                            <div class="lg-card-heading">
                                                <p class="lg-card-name" title="${fn:escapeXml(openLeague.leagueName)}">${fn:escapeXml(openLeague.leagueName)}</p>
                                                <p class="lg-card-sub">${memberCount} / ${openLeague.maxMembers} teams</p>
                                            </div>
                                            <span class="lg-type lg-type-public">Public</span>
                                        </div>

                                        <p class="lg-desc">${fn:escapeXml(openLeague.description)}</p>

                                        <div class="lg-card-footer">
                                            <span class="lg-spots">${spotsLeft} of ${openLeague.maxMembers} spots left</span>
                                            <form class="lg-join-form" action="${pageContext.request.contextPath}/league/join" method="post">
                                                <input type="hidden" name="submit" value="league/join">
                                                <input type="hidden" name="leagueId" value="${fn:escapeXml(openLeague.leagueId)}">
                                                <%-- Disabled without a team: the join would be rejected anyway. --%>
                                                <button type="submit" class="btn-gold lg-join"
                                                        ${spotsLeft <= 0 or not hasTeam ? 'disabled' : ''}
                                                        title="${not hasTeam ? 'Create a team before joining a league' : ''}">Join</button>
                                            </form>
                                        </div>
                                    </article>
                                </c:forEach>
                            </div>
                        </c:otherwise>
                    </c:choose>
                </div>
            </c:otherwise>
        </c:choose>

    </div>
</main>

<script src="${pageContext.request.contextPath}/assets/js/leagues-tabs.js"></script>
</body>
</html>
