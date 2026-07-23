<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>

<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>Fixture Administration - Fantasy TryTons</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/admin-fixtures.css">
</head>
<body class="admin-fixtures">
<%@ include file="/WEB-INF/jspf/navigation.jspf" %>

<main id="adminFixtures">

    <h1>Fixture Administration</h1>
    <p class="page-intro">Review fixtures and update their status as a round progresses.</p>

    <c:if test="${not empty success}">
        <p class="success-message" role="status"><c:out value="${success}" /></p>
    </c:if>

    <c:if test="${not empty error}">
        <p class="error-message" role="alert"><c:out value="${error}" /></p>
    </c:if>

    <c:if test="${not empty fixturesError}">
        <p class="error-message" role="alert"><c:out value="${fixturesError}" /></p>
    </c:if>

    <section id="createFixtureSection">
        <h2>Create Fixture</h2>
        <form method="post" action="${pageContext.request.contextPath}/admin/fixtures" id="createFixtureForm">
            <input type="hidden" name="submit" value="create-fixture">
            <div class="field">
                <label for="createFixtureLeagueId">League</label>
                <select id="createFixtureLeagueId" name="leagueId" required>
                    <option value="">-- Select league --</option>
                    <c:forEach var="league" items="${leagues}">
                        <option value="${league.leagueId}"><c:out value="${league.leagueName}" /></option>
                    </c:forEach>
                </select>
            </div>
            <div class="field">
                <label for="createFixtureRoundId">Round</label>
                <select id="createFixtureRoundId" name="roundId" required>
                    <option value="">-- Select round --</option>
                    <c:forEach var="round" items="${rounds}">
                        <option value="${round.roundId}"><c:out value="${round.season}" /> - Round <c:out value="${round.roundNumber}" /></option>
                    </c:forEach>
                </select>
            </div>
            <div class="field">
                <label for="createFixtureTeamAId">Team A ID</label>
                <input type="text" id="createFixtureTeamAId" name="teamAId" required>
            </div>
            <div class="field">
                <label for="createFixtureTeamBId">Team B ID</label>
                <input type="text" id="createFixtureTeamBId" name="teamBId" required>
            </div>
            <div class="field">
                <label for="createFixtureDate">Fixture date</label>
                <input type="date" id="createFixtureDate" name="fixtureDate" required>
            </div>
            <div class="field">
                <label for="createFixtureTime">Fixture time</label>
                <input type="time" id="createFixtureTime" name="fixtureTime" required>
            </div>
            <button type="submit">Create fixture</button>
        </form>
    </section>

    <section id="fixtureFilterSection">
        <h2>Filter</h2>
        <form action="${pageContext.request.contextPath}/admin/fixtures" method="get" id="fixtureFilterForm">
            <div class="field">
                <label for="statusFilter">Status</label>
                <select id="statusFilter" name="status">
                    <option value="" ${empty statusFilter ? 'selected' : ''}>-- All statuses --</option>
                    <option value="UPCOMING" ${statusFilter eq 'UPCOMING' ? 'selected' : ''}>Upcoming</option>
                    <option value="LOCKED" ${statusFilter eq 'LOCKED' ? 'selected' : ''}>Locked</option>
                    <option value="SIMULATING" ${statusFilter eq 'SIMULATING' ? 'selected' : ''}>Simulating</option>
                    <option value="COMPLETED" ${statusFilter eq 'COMPLETED' ? 'selected' : ''}>Completed</option>
                    <option value="PROCESSED" ${statusFilter eq 'PROCESSED' ? 'selected' : ''}>Processed</option>
                    <option value="CANCELLED" ${statusFilter eq 'CANCELLED' ? 'selected' : ''}>Cancelled</option>
                </select>
            </div>
            <button type="submit">Apply filter</button>
        </form>
    </section>

    <section id="fixtureListSection">
        <h2>Fixtures</h2>
        <c:choose>
            <c:when test="${empty fixtures}">
                <p id="fixturesEmptyState">No fixtures were found.</p>
            </c:when>
            <c:otherwise>
                <table id="fixturesTable">
                    <thead>
                    <tr>
                        <th>League</th>
                        <th>Round</th>
                        <th>Team A</th>
                        <th>Team B</th>
                        <th>Date</th>
                        <th>Status</th>
                        <th>Update status</th>
                    </tr>
                    </thead>
                    <tbody>
                    <c:forEach var="fixture" items="${fixtures}">
                        <tr>
                            <td><c:out value="${fixture.leagueId}" /></td>
                            <td><c:out value="${fixture.roundId}" /></td>
                            <td><c:out value="${fixture.teamAName}" /></td>
                            <td><c:out value="${fixture.teamBName}" /></td>
                            <td><c:out value="${fixture.fixtureDate}" /></td>
                            <td><c:out value="${fixture.fixtureStatus}" /></td>
                            <td>
                                <form method="post" action="${pageContext.request.contextPath}/admin/fixtures" class="statusUpdateForm">
                                    <input type="hidden" name="fixtureId" value="${fixture.fixtureId}">
                                    <select name="status" aria-label="New status">
                                        <option value="UPCOMING" ${fixture.fixtureStatus eq 'UPCOMING' ? 'selected' : ''}>Upcoming</option>
                                        <option value="LOCKED" ${fixture.fixtureStatus eq 'LOCKED' ? 'selected' : ''}>Locked</option>
                                        <option value="SIMULATING" ${fixture.fixtureStatus eq 'SIMULATING' ? 'selected' : ''}>Simulating</option>
                                        <option value="COMPLETED" ${fixture.fixtureStatus eq 'COMPLETED' ? 'selected' : ''}>Completed</option>
                                        <option value="PROCESSED" ${fixture.fixtureStatus eq 'PROCESSED' ? 'selected' : ''}>Processed</option>
                                        <option value="CANCELLED" ${fixture.fixtureStatus eq 'CANCELLED' ? 'selected' : ''}>Cancelled</option>
                                    </select>
                                    <button type="submit">Update</button>
                                </form>
                            </td>
                        </tr>
                    </c:forEach>
                    </tbody>
                </table>
            </c:otherwise>
        </c:choose>
    </section>

</main>
</body>
</html>
