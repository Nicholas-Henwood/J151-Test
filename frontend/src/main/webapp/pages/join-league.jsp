<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>

<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Join League - Fantasy TryTons</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/theme.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/sidebar.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/catalog.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/leagues.css">
</head>
<body class="catalog-page leagues-page">

<c:set var="activeNav" value="leagues" scope="request" />
<%@ include file="/WEB-INF/jspf/sidebar.jspf" %>

<main class="catalog-main" data-catalog data-catalog-noun="league">
    <div class="catalog-content">

        <header class="catalog-header">
            <div>
                <p class="catalog-eyebrow">Fantasy TryTons League</p>
                <h1 class="brand-font">Join a league</h1>
            </div>
            <div class="lg-actions">
                <a class="btn-outline" href="${pageContext.request.contextPath}/leagues">All leagues</a>
            </div>
        </header>

        <c:if test="${not empty error}">
            <p class="catalog-error" role="alert"><c:out value="${error}" /></p>
        </c:if>

        <%@ include file="/WEB-INF/jspf/no-team-notice.jspf" %>

        <%-- ---------- Private league: code only ---------- --%>
        <%-- The code is enough to identify the league, so there is no id to enter.
             Never pre-filled from a URL, and this form always posts. --%>
        <section class="lg-card lg-code-card">
            <div class="lg-card-head">
                <span class="lg-card-icon">
                    <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><rect x="4" y="10" width="16" height="10" rx="2"/><path d="M8 10V7a4 4 0 0 1 8 0v3"/></svg>
                </span>
                <div class="lg-card-heading">
                    <p class="lg-card-name">Have a join code?</p>
                    <p class="lg-card-sub">Private leagues are joined with the code your league manager shared.</p>
                </div>
            </div>

            <form class="lg-code-form" method="post" action="${pageContext.request.contextPath}/league/join">
                <input type="hidden" name="submit" value="league/join">
                <label class="search-wrap" for="leagueCode">
                    <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true"><rect x="4" y="10" width="16" height="10" rx="2"/><path d="M8 10V7a4 4 0 0 1 8 0v3"/></svg>
                    <input type="text" id="leagueCode" name="leagueCode" placeholder="Enter join code"
                           autocomplete="off" aria-label="League join code" required ${not hasTeam ? "disabled" : ""}>
                </label>
                <button type="submit" class="btn-gold lg-join" ${not hasTeam ? "disabled" : ""}>Join</button>
            </form>
        </section>

        <%-- ---------- Public leagues: search by name ---------- --%>
        <div class="lg-tabs" role="presentation">
            <span class="lg-tab is-active">Public leagues<span class="lg-tab-count">${fn:length(publicLeagues)}</span></span>
        </div>

        <div class="catalog-toolbar">
            <label class="search-wrap" for="leagueSearchInput">
                <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true"><circle cx="11" cy="11" r="7"/><path d="M21 21l-4.3-4.3"/></svg>
                <input type="search" id="leagueSearchInput" placeholder="Search league name"
                       autocomplete="off" aria-label="Search league name" data-catalog-search>
            </label>
            <p class="catalog-count" data-catalog-count>${fn:length(publicLeagues)} leagues</p>
        </div>

        <c:choose>
            <c:when test="${empty publicLeagues}">
                <p class="catalog-empty">No public leagues are available right now.</p>
            </c:when>
            <c:otherwise>
                <div class="lg-grid" data-catalog-table data-catalog-body>
                        <c:forEach var="openLeague" items="${publicLeagues}">
                            <c:set var="memberCount" value="${empty memberCounts[openLeague.leagueId] ? 0 : memberCounts[openLeague.leagueId]}" />
                            <c:set var="spotsLeft" value="${openLeague.maxMembers - memberCount}" />

                            <article class="lg-card" data-name="${fn:escapeXml(fn:toLowerCase(openLeague.leagueName))}">
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
                                    <%-- Public leagues need no code; the league is chosen by clicking,
                                         so there is nothing for the user to type or look up. --%>
                                    <form class="lg-join-form" action="${pageContext.request.contextPath}/league/join" method="post">
                                        <input type="hidden" name="submit" value="league/join">
                                        <input type="hidden" name="leagueId" value="${fn:escapeXml(openLeague.leagueId)}">
                                        <button type="submit" class="btn-gold lg-join" ${spotsLeft <= 0 or not hasTeam ? "disabled" : ""}>Join</button>
                                    </form>
                                </div>
                            </article>
                        </c:forEach>
                </div>
                <p class="catalog-empty" data-catalog-empty hidden>No leagues match your search.</p>
            </c:otherwise>
        </c:choose>

    </div>
</main>

<script src="${pageContext.request.contextPath}/assets/js/catalog-filter.js"></script>
</body>
</html>
