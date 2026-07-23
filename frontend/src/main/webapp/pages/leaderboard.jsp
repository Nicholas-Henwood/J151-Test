<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>

<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>${empty leagueName ? 'League' : fn:escapeXml(leagueName)} Standings - Fantasy TryTons</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/theme.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/sidebar.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/catalog.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/leaderboard.css">
</head>
<body class="catalog-page leaderboard-page">

<c:set var="activeNav" value="leaderboards" scope="request" />
<%@ include file="/WEB-INF/jspf/sidebar.jspf" %>

<%-- League (LEAGUE) scope. The league name comes from the servlet; the standings
     response does not carry it. --%>
<c:set var="lbEyebrow" value="League standings" />
<c:set var="lbTitle" value="${empty leagueName ? 'League Standings' : leagueName}" />
<c:set var="lbScope" value="LEAGUE" />
<c:set var="lbOverallHref" value="${pageContext.request.contextPath}/leaderboards" />
<c:set var="lbLeagueHref" value="${pageContext.request.contextPath}/leaderboard?leagueId=${leagueId}" />

<%@ include file="/WEB-INF/jspf/leaderboard-view.jspf" %>

</body>
</html>
