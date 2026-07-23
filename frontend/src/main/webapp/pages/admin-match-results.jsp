<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>

<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>Match Result Capture - Fantasy TryTons</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/admin-match-results.css">
</head>
<body class="admin-match-results">
<%@ include file="/WEB-INF/jspf/navigation.jspf" %>

<main id="adminMatchResults">

    <h1>Match Result Capture</h1>
    <p class="page-intro">
        Select a fixture, capture the team A and team B scores, then record each player's match
        statistics. Fantasy points are calculated by the system and are never entered here.
    </p>

    <%-- Feedback section: safe success / error messages only, never stack traces --%>
    <c:if test="${not empty success}">
        <p class="success-message" role="status">${success}</p>
    </c:if>

    <c:if test="${not empty error}">
        <p class="error-message" role="alert">${error}</p>
    </c:if>

    <c:if test="${not empty fixturesError}">
        <p class="error-message" role="alert">${fixturesError}</p>
    </c:if>

    <%-- Fixture selection: reloads the page (doGet) with the chosen fixture so any --%>
    <%-- previously captured result is loaded for correction. --%>
    <section id="fixtureSelection">
        <h2>Fixture</h2>

        <c:choose>
            <c:when test="${empty fixtures}">
                <p>No fixtures are available to capture right now.</p>
            </c:when>
            <c:otherwise>
                <form action="${pageContext.request.contextPath}/admin/match-results" method="get" id="fixtureSelectForm">
                    <label for="fixtureSelect">Fixture being captured</label>
                    <select id="fixtureSelect" name="fixtureId" required>
                        <option value="">-- Choose a fixture --</option>
                        <c:forEach var="fixture" items="${fixtures}">
                            <option value="${fixture.fixtureId}" ${fixture.fixtureId eq selectedFixtureId ? 'selected' : ''}>
                                <%-- TODO [W4-FE-FIXES-31]: unescaped ${fixture.teamAName}/${fixture.teamBName} (user-chosen names) — stored XSS; also ${playerStatistics.playerId} echoed unescaped at line ~190; use c:out/fn:escapeXml --%>
                                ${fixture.teamAName} vs ${fixture.teamBName}
                                <c:if test="${not empty fixture.fixtureDate}"> (${fixture.fixtureDate})</c:if>
                            </option>
                        </c:forEach>
                    </select>
                    <button type="submit">Load fixture</button>
                </form>
            </c:otherwise>
        </c:choose>
    </section>

    <c:choose>
        <c:when test="${empty selectedFixtureId}">
            <p id="selectPrompt">Choose a fixture above to capture or correct its result.</p>
        </c:when>
        <c:otherwise>

            <%-- Corrections display: existing captured data shown for correction. --%>
            <c:if test="${not empty matchResult}">
                <section id="existingResult">
                    <h2>Currently Captured Result</h2>
                    <table id="existingResultTable">
                        <tbody>
                        <tr><th scope="row">Team A score</th><td>${matchResult.teamAScore}</td></tr>
                        <tr><th scope="row">Team B score</th><td>${matchResult.teamBScore}</td></tr>
                        <c:if test="${not empty matchResult.winnerSide}">
                            <tr><th scope="row">Winner</th><td>${matchResult.winnerSide}</td></tr>
                        </c:if>
                        <tr><th scope="row">Draw</th><td>${matchResult.draw ? 'Yes' : 'No'}</td></tr>
                        <tr><th scope="row">Approved</th><td>${matchResult.approved ? 'Yes' : 'No'}</td></tr>
                        <tr><th scope="row">Current</th><td>${matchResult.current ? 'Yes' : 'No'}</td></tr>
                        <c:if test="${not empty matchResult.resultDate}">
                            <tr><th scope="row">Captured</th><td>${matchResult.resultDate}</td></tr>
                        </c:if>
                        </tbody>
                    </table>
                    <p class="hint">Submitting the form below updates this result.</p>
                </section>
            </c:if>

            <%-- Match result form: team A / team B scores post to the servlet route. --%>
            <section id="resultCapture">
                <h2>Match Result</h2>

                <form action="${pageContext.request.contextPath}/admin/match-results" method="post" id="matchResultForm">
                    <input type="hidden" name="action" value="matchResult">
                    <input type="hidden" name="fixtureId" value="${selectedFixtureId}">

                    <div class="field">
                        <label for="teamAScore">Team A score</label>
                        <input type="number" id="teamAScore" name="teamAScore" min="0" step="1" required
                                value="${not empty matchResult ? matchResult.teamAScore : ''}">
                    </div>

                    <div class="field">
                        <label for="teamBScore">Team B score</label>
                        <input type="number" id="teamBScore" name="teamBScore" min="0" step="1" required
                                value="${not empty matchResult ? matchResult.teamBScore : ''}">
                    </div>

                    <p id="scorePreview" class="hint" aria-live="polite"></p>

                    <div class="field">
                        <label for="simulationReason">Reason / note (optional)</label>
                        <input type="text" id="simulationReason" name="simulationReason"
                                placeholder="e.g. correction after review">
                    </div>

                    <button type="submit">Save match result</button>
                </form>
            </section>

            <%-- Player statistics: per-player statistic inputs for the fixture's squads. --%>
            <%-- Fantasy-point fields are deliberately absent. --%>
            <section id="statisticsCapture">
                <h2>Player Statistics</h2>

                <form action="${pageContext.request.contextPath}/admin/match-results" method="post" id="playerStatisticsForm">
                    <input type="hidden" name="action" value="playerStatistics">
                    <input type="hidden" name="fixtureId" value="${selectedFixtureId}">
                    <input type="hidden" name="resultId" value="${not empty matchResult ? matchResult.resultId : ''}">

                    <div class="field">
                        <label for="teamId">Team ID</label>
                        <input type="text" id="teamId" name="teamId" required>
                    </div>

                    <div class="field">
                        <label for="playerId">Player ID</label>
                        <input type="text" id="playerId" name="playerId" required>
                    </div>

                    <fieldset id="statCounts">
                        <legend>Match statistics</legend>
                        <div class="stat-grid">
                            <div class="field">
                                <label for="tries">Tries</label>
                                <input type="number" id="tries" name="tries" min="0" step="1" value="0">
                            </div>
                            <div class="field">
                                <label for="assists">Assists</label>
                                <input type="number" id="assists" name="assists" min="0" step="1" value="0">
                            </div>
                            <div class="field">
                                <label for="tackles">Tackles</label>
                                <input type="number" id="tackles" name="tackles" min="0" step="1" value="0">
                            </div>
                            <div class="field">
                                <label for="missedTackles">Missed tackles</label>
                                <input type="number" id="missedTackles" name="missedTackles" min="0" step="1" value="0">
                            </div>
                            <div class="field">
                                <label for="conversions">Conversions</label>
                                <input type="number" id="conversions" name="conversions" min="0" step="1" value="0">
                            </div>
                            <div class="field">
                                <label for="penalties">Penalties</label>
                                <input type="number" id="penalties" name="penalties" min="0" step="1" value="0">
                            </div>
                            <div class="field">
                                <label for="metersGained">Metres gained</label>
                                <input type="number" id="metersGained" name="metersGained" min="0" step="1" value="0">
                            </div>
                            <div class="field">
                                <label for="yellowCards">Yellow cards</label>
                                <input type="number" id="yellowCards" name="yellowCards" min="0" step="1" value="0">
                            </div>
                            <div class="field">
                                <label for="redCards">Red cards</label>
                                <input type="number" id="redCards" name="redCards" min="0" step="1" value="0">
                            </div>
                        </div>
                    </fieldset>

                    <button type="submit">Save player statistics</button>
                </form>

                <c:if test="${not empty playerStatistics}">
                    <p class="success-message" role="status">
                        Statistics saved for player ${playerStatistics.playerId}.
                    </p>
                </c:if>
            </section>

        </c:otherwise>
    </c:choose>

</main>

<script src="${pageContext.request.contextPath}/assets/js/admin-match-results.js"></script>
</body>
</html>
