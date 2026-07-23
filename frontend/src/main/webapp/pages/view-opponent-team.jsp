<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="t" tagdir="/WEB-INF/tags" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>

<!DOCTYPE html>
<html lang="en">
    <head>
        <meta charset="UTF-8">
        <meta name="viewport" content="width=device-width, initial-scale=1.0">
        <title>TryTons - Opponent Team</title>
    <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css">
<link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/theme.css">
</head>

    <body>
        <% request.setAttribute("activeNav", "create-team"); %>
        <%@ include file="/WEB-INF/jspf/navigation.jspf" %>
        <main>
            <h1>Opponent Team</h1>

            <c:if test="${not empty error}">
            <p class="error-message" role="alert">
                <c:out value="${error}" />
            </p>
            </c:if>

            <c:if test="${not empty team}">
            <section>
                <h2><c:out value="${team.teamName}" /></h2>
                <table>
                    <tbody>
                        <tr>
                            <th scope="row">Team ID</th>
                            <td><c:out value="${team.teamId}" /></td>
                        </tr>
                        <tr>
                            <th scope="row">Total points</th>
                            <td><c:out value="${team.totalPoints}" /></td>
                        </tr>
                        <tr>
                            <th scope="row">Weekly points</th>
                            <td><c:out value="${team.weeklyPoints}" /></td>
                        </tr>
                    </tbody>
                </table>
            </section>

            <section>
                <h2>Squad</h2>
                <c:choose>
                    <c:when test="${empty team.players}">
                        <p>No players are selected for this team yet.</p>
                    </c:when>
                    <c:otherwise>
                        <table>
                            <thead>
                                <tr>
                                    <th>Player</th>
                                    <th>Position</th>
                                    <th>Club</th>
                                    <th>Value</th>
                                    <th>Squad role</th>
                                    <th>Captain</th>
                                    <th>Vice-captain</th>
                                    <th>Active</th>
                                    <th>Fantasy points</th>
                                    <th>Attacking</th>
                                    <th>Defensive</th>
                                    <th>Kicking</th>
                                    <th>Discipline</th>
                                    <th>Consistency</th>
                                    <th>Fitness</th>
                                    <th>Form</th>
                                </tr>
                            </thead>
                            <tbody>
                                <c:forEach var="selection" items="${team.players}">
                                <tr>
                                    <td><c:out value="${selection.playerName}" /></td>
                                    <td><c:out value="${selection.positionName}" /></td>
                                    <td><c:out value="${selection.clubName}" /></td>
                                    <td><t:money value="${selection.value}" /></td>
                                    <td><c:out value="${selection.squadRole}" /></td>
                                    <td>${selection.isCaptain ? 'Yes' : 'No'}</td>
                                    <td>${selection.isViceCaptain ? 'Yes' : 'No'}</td>
                                    <td>${selection.isActive ? 'Yes' : 'No'}</td>
                                    <td><c:out value="${selection.totalFantasyPoints}" /></td>
                                    <td><t:rating value="${selection.attackingAbility}" /></td>
                                    <td><t:rating value="${selection.defensiveAbility}" /></td>
                                    <td><t:rating value="${selection.kickingAbility}" /></td>
                                    <td><t:rating value="${selection.discipline}" /></td>
                                    <td><t:rating value="${selection.consistency}" /></td>
                                    <td><t:rating value="${selection.fitness}" /></td>
                                    <td><t:rating value="${selection.currentForm}" /></td>
                                </tr>
                                </c:forEach>
                            </tbody>
                        </table>
                    </c:otherwise>
                </c:choose>
            </section>
            </c:if>
        </main>
    </body>
</html>
