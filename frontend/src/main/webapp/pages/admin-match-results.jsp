<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>

<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Match Result Capture - Fantasy TryTons</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/theme.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/sidebar.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/catalog.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/admin-match-results.css">
</head>
<body class="catalog-page amr-page">

<c:set var="activeNav" value="admin-match-results" scope="request" />
<%@ include file="/WEB-INF/jspf/sidebar.jspf" %>

<main class="catalog-main" id="adminMatchResults">
    <div class="catalog-content">

        <header class="catalog-header">
            <div>
                <p class="catalog-eyebrow">Administration</p>
                <h1 class="brand-font">Match Result Capture</h1>
                <p class="amr-intro">
                    Select a fixture, capture the team A and team B scores, then record each player's match
                    statistics. Fantasy points are calculated by the system and are never entered here.
                </p>
            </div>
        </header>

        <%-- Feedback: safe success / error messages only, never stack traces. --%>
        <c:if test="${not empty success}">
            <p class="amr-alert amr-alert-success" role="status"><c:out value="${success}" /></p>
        </c:if>
        <c:if test="${not empty error}">
            <p class="amr-alert amr-alert-error" role="alert"><c:out value="${error}" /></p>
        </c:if>
        <c:if test="${not empty fixturesError}">
            <p class="amr-alert amr-alert-error" role="alert"><c:out value="${fixturesError}" /></p>
        </c:if>

        <%-- ---------- Fixture selector ---------- --%>
        <%-- Reloads the page (doGet) with the chosen fixture so any previously
             captured result is loaded for correction. --%>
        <section class="amr-panel" id="fixtureSelection">
            <h2 class="amr-panel-title">Fixture</h2>

            <c:choose>
                <c:when test="${empty fixtures}">
                    <p class="amr-empty">No fixtures are available to capture right now.</p>
                </c:when>
                <c:otherwise>
                    <form action="${pageContext.request.contextPath}/admin/match-results" method="get" id="fixtureSelectForm" class="amr-fixture-form">
                        <div class="amr-field amr-field-grow">
                            <label class="amr-label" for="fixtureSelect">Fixture being captured</label>
                            <div class="amr-select-wrap">
                                <select class="amr-select" id="fixtureSelect" name="fixtureId" required>
                                    <option value="">&mdash; Choose a fixture &mdash;</option>
                                    <c:forEach var="fixture" items="${fixtures}">
                                        <%-- selectedFixtureId is the raw request parameter (a String), while
                                             fixture.fixtureId is a UUID, so compare their string forms. --%>
                                        <option value="${fixture.fixtureId}" ${fixture.fixtureId.toString() eq selectedFixtureId ? 'selected' : ''}>${fn:escapeXml(fixture.teamAName)} vs ${fn:escapeXml(fixture.teamBName)}<c:if test="${not empty fixture.fixtureDate}"> (${fixture.fixtureDate})</c:if></option>
                                    </c:forEach>
                                </select>
                                <svg class="amr-select-caret" width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.2" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true"><path d="M6 9l6 6 6-6"/></svg>
                            </div>
                        </div>
                        <button type="submit" class="btn-gold amr-load">Load fixture</button>
                    </form>
                </c:otherwise>
            </c:choose>
        </section>

        <c:choose>
            <c:when test="${empty selectedFixtureId}">
                <div class="amr-prompt" id="selectPrompt">
                    <span class="amr-prompt-icon" aria-hidden="true">
                        <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.7" stroke-linecap="round" stroke-linejoin="round"><rect x="3" y="4" width="18" height="18" rx="2"/><path d="M16 2v4"/><path d="M8 2v4"/><path d="M3 10h18"/></svg>
                    </span>
                    <p>Choose a fixture above to capture or correct its result.</p>
                </div>
            </c:when>

            <c:otherwise>
                <div class="amr-grid">

                    <%-- ================= Left column ================= --%>
                    <div class="amr-col">

                        <%-- Existing captured data, shown for correction. --%>
                        <c:if test="${not empty matchResult}">
                            <section class="amr-panel" id="existingResult">
                                <h2 class="amr-panel-title">Currently captured result</h2>
                                <dl class="amr-kv">
                                    <div class="amr-kv-row"><dt>Team A score</dt><dd class="amr-kv-num">${matchResult.teamAScore}</dd></div>
                                    <div class="amr-kv-row"><dt>Team B score</dt><dd class="amr-kv-num">${matchResult.teamBScore}</dd></div>
                                    <c:if test="${not empty matchResult.winnerSide}">
                                        <div class="amr-kv-row"><dt>Winner</dt><dd>${fn:escapeXml(matchResult.winnerSide)}</dd></div>
                                    </c:if>
                                    <div class="amr-kv-row"><dt>Draw</dt><dd><span class="amr-flag ${matchResult.draw ? 'is-yes' : 'is-no'}">${matchResult.draw ? 'Yes' : 'No'}</span></dd></div>
                                    <div class="amr-kv-row"><dt>Approved</dt><dd><span class="amr-flag ${matchResult.approved ? 'is-yes' : 'is-no'}">${matchResult.approved ? 'Yes' : 'No'}</span></dd></div>
                                    <div class="amr-kv-row"><dt>Current</dt><dd><span class="amr-flag ${matchResult.current ? 'is-yes' : 'is-no'}">${matchResult.current ? 'Yes' : 'No'}</span></dd></div>
                                    <c:if test="${not empty matchResult.resultDate}">
                                        <div class="amr-kv-row"><dt>Captured</dt><dd>${fn:substring(fn:replace(matchResult.resultDate, 'T', ' '), 0, 16)}</dd></div>
                                    </c:if>
                                </dl>
                                <p class="amr-hint">Submitting the form below updates this result.</p>
                            </section>
                        </c:if>

                        <%-- Match result form. --%>
                        <section class="amr-panel" id="resultCapture">
                            <h2 class="amr-panel-title">Match result</h2>

                            <form action="${pageContext.request.contextPath}/admin/match-results" method="post" id="matchResultForm">
                                <input type="hidden" name="action" value="matchResult">
                                <input type="hidden" name="fixtureId" value="${fn:escapeXml(selectedFixtureId)}">

                                <div class="amr-row-2">
                                    <div class="amr-field">
                                        <label class="amr-label" for="teamAScore">Team A score</label>
                                        <input class="amr-input" type="number" id="teamAScore" name="teamAScore" min="0" step="1" required
                                               value="${not empty matchResult ? matchResult.teamAScore : ''}">
                                    </div>
                                    <div class="amr-field">
                                        <label class="amr-label" for="teamBScore">Team B score</label>
                                        <input class="amr-input" type="number" id="teamBScore" name="teamBScore" min="0" step="1" required
                                               value="${not empty matchResult ? matchResult.teamBScore : ''}">
                                    </div>
                                </div>

                                <%-- Live winner preview, filled by admin-match-results.js. --%>
                                <div class="amr-preview" id="scorePreview" aria-live="polite" hidden>
                                    <span class="amr-preview-score" id="scorePreviewScore"></span>
                                    <span class="amr-preview-outcome" id="scorePreviewOutcome"></span>
                                </div>

                                <div class="amr-field">
                                    <label class="amr-label" for="simulationReason">Reason / note (optional)</label>
                                    <input class="amr-input" type="text" id="simulationReason" name="simulationReason"
                                           placeholder="e.g. correction after review">
                                </div>

                                <button type="submit" class="btn-gold amr-submit">Save match result</button>
                            </form>
                        </section>
                    </div>

                    <%-- ================= Right column ================= --%>
                    <div class="amr-col">
                        <%-- Fantasy-point fields are deliberately absent: the system calculates them. --%>
                        <section class="amr-panel" id="statisticsCapture">
                            <h2 class="amr-panel-title">Player statistics</h2>

                            <c:if test="${not empty playerStatistics}">
                                <p class="amr-alert amr-alert-success" role="status">
                                    Player statistics saved.
                                </p>
                            </c:if>

                            <form action="${pageContext.request.contextPath}/admin/match-results" method="post" id="playerStatisticsForm">
                                <input type="hidden" name="action" value="playerStatistics">
                                <input type="hidden" name="fixtureId" value="${fn:escapeXml(selectedFixtureId)}">
                                <input type="hidden" name="resultId" value="${not empty matchResult ? matchResult.resultId : ''}">

                                <div class="amr-row-2">
                                    <div class="amr-field">
                                        <label class="amr-label" for="teamId">Team</label>
                                        <div class="amr-select-wrap">
                                            <select class="amr-select" id="teamId" name="teamId" required>
                                                <option value="">&mdash; Choose a team &mdash;</option>
                                                <c:if test="${not empty selectedFixture}">
                                                    <option value="${selectedFixture.teamAId}">${fn:escapeXml(selectedFixture.teamAName)}</option>
                                                    <option value="${selectedFixture.teamBId}">${fn:escapeXml(selectedFixture.teamBName)}</option>
                                                </c:if>
                                            </select>
                                            <svg class="amr-select-caret" width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.2" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true"><path d="M6 9l6 6 6-6"/></svg>
                                        </div>
                                    </div>
                                    <div class="amr-field">
                                        <label class="amr-label" for="playerId">Player</label>
                                        <div class="amr-select-wrap">
                                            <select class="amr-select" id="playerId" name="playerId" required>
                                                <option value="">&mdash; Choose a team first &mdash;</option>
                                                <c:if test="${not empty selectedFixture}">
                                                    <c:forEach var="rosterPlayer" items="${teamAPlayers}">
                                                        <option value="${rosterPlayer.playerId}" data-team-id="${selectedFixture.teamAId}">${fn:escapeXml(rosterPlayer.playerName)}</option>
                                                    </c:forEach>
                                                    <c:forEach var="rosterPlayer" items="${teamBPlayers}">
                                                        <option value="${rosterPlayer.playerId}" data-team-id="${selectedFixture.teamBId}">${fn:escapeXml(rosterPlayer.playerName)}</option>
                                                    </c:forEach>
                                                </c:if>
                                            </select>
                                            <svg class="amr-select-caret" width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.2" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true"><path d="M6 9l6 6 6-6"/></svg>
                                        </div>
                                    </div>
                                </div>

                                <fieldset class="amr-fieldset" id="statCounts">
                                    <legend class="amr-legend">Match statistics</legend>
                                    <div class="amr-stat-grid">
                                        <div class="amr-field">
                                            <label class="amr-label" for="tries">Tries</label>
                                            <input class="amr-input" type="number" id="tries" name="tries" min="0" step="1" value="0">
                                        </div>
                                        <div class="amr-field">
                                            <label class="amr-label" for="assists">Assists</label>
                                            <input class="amr-input" type="number" id="assists" name="assists" min="0" step="1" value="0">
                                        </div>
                                        <div class="amr-field">
                                            <label class="amr-label" for="tackles">Tackles</label>
                                            <input class="amr-input" type="number" id="tackles" name="tackles" min="0" step="1" value="0">
                                        </div>
                                        <div class="amr-field">
                                            <label class="amr-label" for="missedTackles">Missed tackles</label>
                                            <input class="amr-input" type="number" id="missedTackles" name="missedTackles" min="0" step="1" value="0">
                                        </div>
                                        <div class="amr-field">
                                            <label class="amr-label" for="conversions">Conversions</label>
                                            <input class="amr-input" type="number" id="conversions" name="conversions" min="0" step="1" value="0">
                                        </div>
                                        <div class="amr-field">
                                            <label class="amr-label" for="penalties">Penalties</label>
                                            <input class="amr-input" type="number" id="penalties" name="penalties" min="0" step="1" value="0">
                                        </div>
                                        <div class="amr-field">
                                            <label class="amr-label" for="metersGained">Metres gained</label>
                                            <input class="amr-input" type="number" id="metersGained" name="metersGained" min="0" step="1" value="0">
                                        </div>
                                        <div class="amr-field">
                                            <label class="amr-label" for="yellowCards">Yellow cards</label>
                                            <input class="amr-input" type="number" id="yellowCards" name="yellowCards" min="0" step="1" value="0">
                                        </div>
                                        <div class="amr-field">
                                            <label class="amr-label" for="redCards">Red cards</label>
                                            <input class="amr-input" type="number" id="redCards" name="redCards" min="0" step="1" value="0">
                                        </div>
                                    </div>
                                </fieldset>

                                <button type="submit" class="btn-gold amr-submit">Save player statistics</button>
                            </form>
                        </section>
                    </div>

                </div>
            </c:otherwise>
        </c:choose>

    </div>
</main>

<script src="${pageContext.request.contextPath}/assets/js/admin-match-results.js"></script>
</body>
</html>
