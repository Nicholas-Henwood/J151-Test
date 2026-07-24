<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>

<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Create League - Fantasy TryTons</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/theme.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/sidebar.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/catalog.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/create-league.css">
</head>
<body class="catalog-page cl-page">

<c:set var="activeNav" value="leagues" scope="request" />
<%@ include file="/WEB-INF/jspf/sidebar.jspf" %>

<main class="catalog-main">
    <div class="catalog-content">

        <a class="cl-back" href="${pageContext.request.contextPath}/leagues">&larr; Back to leagues</a>

        <header class="catalog-header">
            <div>
                <p class="catalog-eyebrow">Leagues</p>
                <h1 class="brand-font">Create League</h1>
            </div>
        </header>

        <c:if test="${not empty error}">
            <p class="cl-alert" role="alert"><c:out value="${error}" /></p>
        </c:if>

        <c:choose>
            <%-- Creating a league enrols the creator as its first member and manager, and a
                 membership needs a team (leagueMembership.teamId is NOT NULL). The backend
                 rejects the attempt outright, so the form is withheld rather than letting it
                 be filled in and fail on submit. --%>
            <c:when test="${not hasTeam}">
                <div class="cl-notice">
                    <span class="cl-notice-icon" aria-hidden="true">
                        <svg width="26" height="26" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.7" stroke-linecap="round" stroke-linejoin="round"><path d="M17 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2"/><circle cx="9" cy="7" r="4"/><path d="M23 21v-2a4 4 0 0 0-3-3.87"/><path d="M16 3.13a4 4 0 0 1 0 7.75"/></svg>
                    </span>
                    <h2>You need a team first</h2>
                    <p>Creating a league signs you up as its first member and manager &mdash; and that needs a fantasy team.</p>
                    <a class="btn-gold cl-notice-cta" href="${pageContext.request.contextPath}/create-team">Create your team &rarr;</a>
                </div>
            </c:when>

            <c:otherwise>
                <%-- A successful create redirects straight to /league?leagueId=... (PRG), so
                     this page only ever renders the form (with an error on failure). --%>
                <form class="cl-card" method="post" action="${pageContext.request.contextPath}/league/create" id="createLeagueForm">
                    <input type="hidden" name="submit" value="league/create" />

                    <div class="cl-field">
                        <label class="cl-label" for="leagueName">League name</label>
                        <input class="cl-input" type="text" id="leagueName" name="leagueName"
                               value="${fn:escapeXml(param.leagueName)}" placeholder="e.g. Highveld Heroes"
                               maxlength="100" required>
                    </div>

                    <div class="cl-field">
                        <label class="cl-label" for="description">Description</label>
                        <textarea class="cl-textarea" id="description" name="description" rows="3"
                                  placeholder="What's this league about?" required><c:out value="${param.description}" /></textarea>
                    </div>

                    <div class="cl-row">
                        <div class="cl-field">
                            <label class="cl-label" for="leagueType">Visibility</label>
                            <div class="cl-select-wrap">
                                <select class="cl-select" id="leagueType" name="leagueType" required>
                                    <option value="PUBLIC" ${param.leagueType == 'PUBLIC' ? 'selected' : ''}>Public</option>
                                    <option value="PRIVATE" ${param.leagueType == 'PRIVATE' ? 'selected' : ''}>Private</option>
                                </select>
                                <svg class="cl-select-caret" width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.2" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true"><path d="M6 9l6 6 6-6"/></svg>
                            </div>
                            <p class="cl-help">Public leagues anyone can find and join. Private leagues need an invite code.</p>
                        </div>

                        <div class="cl-field">
                            <label class="cl-label" for="maxMembers">Max members</label>
                            <input class="cl-input" type="number" id="maxMembers" name="maxMembers" min="1" max="100"
                                   value="${empty param.maxMembers ? 20 : fn:escapeXml(param.maxMembers)}" required>
                            <p class="cl-help">Up to how many teams can join, including yours.</p>
                        </div>
                    </div>

                    <button type="submit" name="submit" value="league/create" class="btn-gold cl-submit">Create league</button>
                </form>
            </c:otherwise>
        </c:choose>

    </div>
</main>

</body>
</html>
