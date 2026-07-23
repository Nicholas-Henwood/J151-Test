<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>Fixture Details - Fantasy TryTons</title>
<link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css">
<link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/theme.css">
</head>
<body>
<%@ include file="/WEB-INF/jspf/navigation.jspf" %>

<h1>Fixture Details</h1>

<c:if test="${not empty error}">
    <p class="error-message" role="alert"><c:out value ="${error}" /></p>
</c:if>

<c:if test="${not empty fixture}">
    <table id="fixtureDetailsTable">
        <tr>
            <th>Matchup</th>
            <td><c:out value="${fixture.teamAName}" /> vs <c:out value="${fixture.teamBName}" /></td>
        </tr>
        <tr>
            <th>Date</th>
            <td>${fixture.fixtureDate}</td>
        </tr>
        <tr>
            <th>Time</th>
            <td>${fixture.fixtureTime}</td>
        </tr>
        <tr>
            <th>Status</th>
            <td>${fixture.fixtureStatus}</td>
        </tr>
    </table>

    <c:if test="${fixture.fixtureStatus == 'COMPLETED'}">
        <p id="fixtureResult">Simulated on ${fixture.simulationDate}</p>
    </c:if>
</c:if>

<c:if test="${not empty matchResult}">
    <h2>Match Result</h2>
    <table id="matchResultTable">
        <tr>
            <th>Score</th>
            <td><c:out value="${fixture.teamAName}" /> ${matchResult.teamAScore} - ${matchResult.teamBScore} <c:out value="${fixture.teamBName}" /></td>
        </tr>
        <tr>
            <th>Winner</th>
            <td>${matchResult.draw ? 'Draw' : matchResult.winnerSide}</td>
        </tr>
        <tr>
            <th>Approved</th>
            <td>${matchResult.approved}</td>
        </tr>
        <tr>
            <th>Result Date</th>
            <td>${matchResult.resultDate}</td>
        </tr>
    </table>
</c:if>

<c:if test="${not empty teamScores}">
    <h2>Team Side Scores</h2>
    <table id="teamScoresTable">
        <tr>
            <th>Team Side</th>
            <th>Player Points</th>
            <th>Captain Bonus</th>
            <th>Transfer Penalty</th>
            <th>Total Score</th>
            <th>Calculated At</th>
        </tr>
        <c:forEach var="teamScore" items="${teamScores}">
            <tr>
                <td>${teamScore.teamSide}</td>
                <td>${teamScore.playerPoints}</td>
                <td>${teamScore.captainBonus}</td>
                <td>${teamScore.transferPenalty}</td>
                <td>${teamScore.totalScore}</td>
                <td>${teamScore.calculatedAt}</td>
            </tr>
        </c:forEach>
    </table>
</c:if>

<c:if test="${not empty playerStats}">
    <h2>Player Statistics</h2>
    <table id="playerStatsTable">
        <tr>
            <th>Player</th>
            <th>Tries</th>
            <th>Assists</th>
            <th>Tackles</th>
            <th>Missed Tackles</th>
            <th>Conversions</th>
            <th>Penalties</th>
            <th>Meters Gained</th>
            <th>Yellow Cards</th>
            <th>Red Cards</th>
            <th>Points</th>
        </tr>
        <c:forEach var="playerStat" items="${playerStats}">
            <tr>
                <td>${playerStat.playerId}</td>
                <td>${playerStat.tries}</td>
                <td>${playerStat.assists}</td>
                <td>${playerStat.tackles}</td>
                <td>${playerStat.missedTackles}</td>
                <td>${playerStat.conversions}</td>
                <td>${playerStat.penalties}</td>
                <td>${playerStat.metersGained}</td>
                <td>${playerStat.yellowCards}</td>
                <td>${playerStat.redCards}</td>
                <td>
                    <a href="${pageContext.request.contextPath}/fixture?submit=fixture&fixtureId=${fixture.fixtureId}&statId=${playerStat.statId}">
                        View Points Breakdown
                    </a>
                </td>
            </tr>
        </c:forEach>
    </table>
</c:if>

<c:if test="${not empty selectedPoints}">
    <h2>Points Breakdown</h2>
    <p id="selectedPointsTotal">Total Points: ${selectedPoints.totalPoints} (${selectedPoints.final ? 'Final' : 'Provisional'})</p>
    <c:if test="${not empty breakdowns}">
        <table id="pointsBreakdownTable">
            <tr>
                <th>Category</th>
                <th>Points</th>
                <th>Description</th>
            </tr>
            <c:forEach var="breakdown" items="${breakdowns}">
                <tr>
                    <td>${breakdown.category}</td>
                    <td>${breakdown.points}</td>
                    <td>${breakdown.description}</td>
                </tr>
            </c:forEach>
        </table>
    </c:if>
</c:if>

<p>
        <a href="${pageContext.request.contextPath}/fixtures?submit=fixtures" id="backToFixturesLink">
            Back to Fixtures
        </a>
</p>

</body>
</html>