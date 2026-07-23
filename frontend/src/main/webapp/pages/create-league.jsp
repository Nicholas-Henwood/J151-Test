<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>

<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>Create League - Fantasy TryTons</title>
<link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css">
<link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/theme.css">
</head>
<body>
<%@ include file="/WEB-INF/jspf/navigation.jspf" %>

<h1>Create League</h1>

<c:if test="${not empty error}">
    <p class="error-message" role="alert"><c:out value="${error}" /></p>
</c:if>

<%-- Creating a league enrols the creator as its first member and manager, and a
     membership needs a team (leagueMembership.teamId is NOT NULL). The backend
     rejects the attempt outright, so the form is withheld rather than letting it
     be filled in and fail on submit. --%>
<c:if test="${not hasTeam}">
    <p class="error-message" role="alert">
        You need a fantasy team before you can create a league.
        <a href="${pageContext.request.contextPath}/create-team">Create your team first</a>.
    </p>
</c:if>

<c:if test="${hasTeam}">
<%-- A successful create now redirects straight to /league?leagueId=... (PRG) rather than forwarding
    back here, so this page only ever needs to render the form (with an error on failure). --%>
<form method="post" action="${pageContext.request.contextPath}/league/create" id="createLeagueForm">
    <input type="hidden" name="submit" value="league/create" />

    <div>
        <label for="leagueName">League name</label>
        <input type="text" id="leagueName" name="leagueName"
                value="${param.leagueName}" required>
    </div>
    <br>

    <div>
        <label for="description">Description</label>
        <textarea id="description" name="description" required><c:out value="${param.description}" /></textarea>
    </div>
    <br>

    <div>
        <label for="leagueType">Visibility</label>
        <select id="leagueType" name="leagueType" required>
            <option value="PUBLIC" ${param.leagueType == 'PUBLIC' ? 'selected' : ''}>Public</option>
            <option value="PRIVATE" ${param.leagueType == 'PRIVATE' ? 'selected' : ''}>Private</option>
        </select>
    </div>
    <br>

    <div>
        <label for="maxMembers">Max members</label>
        <input type="number" id="maxMembers" name="maxMembers" min="1"
                value="${empty param.maxMembers ? 20 : param.maxMembers}" required>
    </div>
    <br>

    <button type="submit" name="submit" value="league/create">Create league</button>
</form>
</c:if>

</body>
</html>
