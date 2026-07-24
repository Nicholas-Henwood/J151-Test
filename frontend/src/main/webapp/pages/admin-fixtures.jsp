<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>

<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Fixture Administration - Fantasy TryTons</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/theme.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/sidebar.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/catalog.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/admin-fixtures.css">
</head>
<body class="catalog-page afx-page">

<c:set var="activeNav" value="admin-fixtures" scope="request" />
<%@ include file="/WEB-INF/jspf/sidebar.jspf" %>

<c:set var="statusOptions" value="UPCOMING,LOCKED,SIMULATING,COMPLETED,PROCESSED,CANCELLED" />

<main class="catalog-main" id="adminFixtures">
    <div class="catalog-content">

        <header class="catalog-header">
            <div>
                <p class="catalog-eyebrow">Administration</p>
                <h1 class="brand-font">Fixture Administration</h1>
                <p class="afx-intro">Review fixtures and update their status as a round progresses.</p>
            </div>
        </header>

        <c:if test="${not empty success}">
            <p class="afx-alert afx-alert-success" role="status"><c:out value="${success}" /></p>
        </c:if>
        <c:if test="${not empty error}">
            <p class="afx-alert afx-alert-error" role="alert"><c:out value="${error}" /></p>
        </c:if>
        <c:if test="${not empty fixturesError}">
            <p class="afx-alert afx-alert-error" role="alert"><c:out value="${fixturesError}" /></p>
        </c:if>

        <div class="afx-grid">

            <%-- ================= Left: filter + fixtures ================= --%>
            <div class="afx-col">

                <section id="fixtureListSection">
                    <div class="afx-section-head">
                        <h2 class="afx-section-title">Fixtures</h2>
                        <span class="afx-rule-line"></span>
                        <span class="afx-count">${fn:length(fixtures)} fixture${fn:length(fixtures) == 1 ? '' : 's'}</span>
                    </div>

                    <form action="${pageContext.request.contextPath}/admin/fixtures" method="get" id="fixtureFilterForm" class="afx-filter">
                        <div class="afx-select-wrap afx-filter-select">
                            <select class="afx-select" id="statusFilter" name="status" aria-label="Filter by status">
                                <option value="" ${empty statusFilter ? 'selected' : ''}>All statuses</option>
                                <c:forEach var="s" items="${fn:split(statusOptions, ',')}">
                                    <option value="${s}" ${statusFilter eq s ? 'selected' : ''}>${fn:substring(s, 0, 1)}${fn:toLowerCase(fn:substring(s, 1, fn:length(s)))}</option>
                                </c:forEach>
                            </select>
                            <svg class="afx-select-caret" width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.2" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true"><path d="M6 9l6 6 6-6"/></svg>
                        </div>
                        <button type="submit" class="afx-ghost">Apply filter</button>
                    </form>

                    <c:choose>
                        <c:when test="${empty fixtures}">
                            <p class="afx-empty" id="fixturesEmptyState">No fixtures were found.</p>
                        </c:when>
                        <c:otherwise>
                            <div class="ftbl">
                                <div class="ftbl-scroll">
                                    <div class="ftbl-head">
                                        <span>League</span>
                                        <span>Round</span>
                                        <span>Team A</span>
                                        <span>Team B</span>
                                        <span>Date</span>
                                        <span>Status</span>
                                        <span>Update status</span>
                                    </div>

                                    <c:forEach var="fixture" items="${fixtures}">
                                        <c:set var="fxStatus" value="${empty fixture.fixtureStatus ? 'UPCOMING' : fixture.fixtureStatus}" />
                                        <div class="ftbl-row">
                                            <span class="ftbl-league">${empty leagueNamesById[fixture.leagueId.toString()] ? '&mdash;' : fn:escapeXml(leagueNamesById[fixture.leagueId.toString()])}</span>
                                            <span class="ftbl-round">${empty roundLabelsById[fixture.roundId.toString()] ? '&mdash;' : fn:escapeXml(roundLabelsById[fixture.roundId.toString()])}</span>
                                            <span class="ftbl-team" title="${fn:escapeXml(fixture.teamAName)}"><c:out value="${fixture.teamAName}" /></span>
                                            <span class="ftbl-team" title="${fn:escapeXml(fixture.teamBName)}"><c:out value="${fixture.teamBName}" /></span>
                                            <span class="ftbl-date">${fixture.fixtureDate}</span>
                                            <span><span class="afx-pill afx-pill-${fn:toLowerCase(fxStatus)}">${fn:substring(fxStatus, 0, 1)}${fn:toLowerCase(fn:substring(fxStatus, 1, fn:length(fxStatus)))}</span></span>
                                            <span class="ftbl-update">
                                                <%-- Only offer transitions the backend allows for the current status; a
                                                     terminal status (processed / cancelled) has none. --%>
                                                <c:set var="nextStatuses" value="${statusTransitions[fxStatus]}" />
                                                <c:choose>
                                                    <c:when test="${empty nextStatuses}">
                                                        <span class="afx-update-none">No further changes</span>
                                                    </c:when>
                                                    <c:otherwise>
                                                        <form method="post" action="${pageContext.request.contextPath}/admin/fixtures" class="statusUpdateForm afx-update-form">
                                                            <input type="hidden" name="fixtureId" value="${fixture.fixtureId}">
                                                            <div class="afx-select-wrap afx-update-select">
                                                                <select class="afx-select afx-select-sm" name="status" aria-label="New status">
                                                                    <c:forEach var="s" items="${nextStatuses}">
                                                                        <option value="${s}">${fn:substring(s, 0, 1)}${fn:toLowerCase(fn:substring(s, 1, fn:length(s)))}</option>
                                                                    </c:forEach>
                                                                </select>
                                                                <svg class="afx-select-caret" width="13" height="13" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.2" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true"><path d="M6 9l6 6 6-6"/></svg>
                                                            </div>
                                                            <button type="submit" class="afx-update-btn">Update</button>
                                                        </form>
                                                    </c:otherwise>
                                                </c:choose>
                                            </span>
                                        </div>
                                    </c:forEach>
                                </div>
                            </div>
                        </c:otherwise>
                    </c:choose>
                </section>
            </div>

            <%-- ================= Right: create fixture ================= --%>
            <section class="afx-panel" id="createFixtureSection">
                <h2 class="afx-panel-title">Create fixture</h2>
                <form method="post" action="${pageContext.request.contextPath}/admin/fixtures" id="createFixtureForm">
                    <input type="hidden" name="submit" value="create-fixture">

                    <div class="afx-field">
                        <label class="afx-label" for="createFixtureLeagueId">League</label>
                        <div class="afx-select-wrap">
                            <select class="afx-select" id="createFixtureLeagueId" name="leagueId" required>
                                <option value="">&mdash; Select league &mdash;</option>
                                <c:forEach var="league" items="${leagues}">
                                    <option value="${league.leagueId}"><c:out value="${league.leagueName}" /></option>
                                </c:forEach>
                            </select>
                            <svg class="afx-select-caret" width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.2" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true"><path d="M6 9l6 6 6-6"/></svg>
                        </div>
                    </div>

                    <div class="afx-field">
                        <label class="afx-label" for="createFixtureRoundId">Round</label>
                        <div class="afx-select-wrap">
                            <select class="afx-select" id="createFixtureRoundId" name="roundId" required>
                                <option value="">&mdash; Select round &mdash;</option>
                                <c:forEach var="round" items="${rounds}">
                                    <option value="${round.roundId}"><c:out value="${round.season}" /> &middot; Round <c:out value="${round.roundNumber}" /></option>
                                </c:forEach>
                            </select>
                            <svg class="afx-select-caret" width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.2" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true"><path d="M6 9l6 6 6-6"/></svg>
                        </div>
                    </div>

                    <div class="afx-field">
                        <label class="afx-label" for="createFixtureTeamAId">Team A</label>
                        <div class="afx-select-wrap">
                            <select class="afx-select" id="createFixtureTeamAId" name="teamAId" required>
                                <option value="">&mdash; Select league first &mdash;</option>
                                <c:forEach var="league" items="${leagues}">
                                    <c:forEach var="member" items="${teamsByLeagueId[league.leagueId]}">
                                        <option value="${member.teamId}" data-league-id="${league.leagueId}"><c:out value="${member.teamDisplayName}" /></option>
                                    </c:forEach>
                                </c:forEach>
                            </select>
                            <svg class="afx-select-caret" width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.2" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true"><path d="M6 9l6 6 6-6"/></svg>
                        </div>
                    </div>

                    <div class="afx-field">
                        <label class="afx-label" for="createFixtureTeamBId">Team B</label>
                        <div class="afx-select-wrap">
                            <select class="afx-select" id="createFixtureTeamBId" name="teamBId" required>
                                <option value="">&mdash; Select league first &mdash;</option>
                                <c:forEach var="league" items="${leagues}">
                                    <c:forEach var="member" items="${teamsByLeagueId[league.leagueId]}">
                                        <option value="${member.teamId}" data-league-id="${league.leagueId}"><c:out value="${member.teamDisplayName}" /></option>
                                    </c:forEach>
                                </c:forEach>
                            </select>
                            <svg class="afx-select-caret" width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.2" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true"><path d="M6 9l6 6 6-6"/></svg>
                        </div>
                    </div>

                    <%-- League -> active teams map, consumed by the script below to
                         populate the team dropdowns when a league is chosen. Held in a
                         hidden input so HTML attribute escaping keeps the JSON safe. --%>
                    <input type="hidden" id="afxTeamsByLeague" value="${fn:escapeXml(teamsByLeagueJson)}">


                    <div class="afx-row-2">
                        <div class="afx-field">
                            <label class="afx-label" for="createFixtureDate">Date</label>
                            <input class="afx-input" type="date" id="createFixtureDate" name="fixtureDate" required>
                        </div>
                        <div class="afx-field">
                            <label class="afx-label" for="createFixtureTime">Time</label>
                            <input class="afx-input" type="time" id="createFixtureTime" name="fixtureTime" required>
                        </div>
                    </div>

                    <button type="submit" class="btn-gold afx-submit">Create fixture</button>
                </form>
            </section>

        </div>

    </div>
</main>

<script src="${pageContext.request.contextPath}/assets/js/admin-fixtures.js"></script>
</body>
</html>
