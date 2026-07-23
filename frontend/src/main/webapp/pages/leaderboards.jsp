<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>

<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Leaderboards - Fantasy TryTons</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/theme.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/sidebar.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/catalog.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/leaderboard.css">
</head>
<body class="catalog-page leaderboard-page">

<c:set var="activeNav" value="leaderboards" scope="request" />
<%@ include file="/WEB-INF/jspf/sidebar.jspf" %>

<%-- Overall (MASTER) scope. Season isn't on the standings response, so it is fixed
     to match the rest of the redesign; the heading is the constant table name. --%>
<c:set var="lbEyebrow" value="Season 2025/26 standings" />
<c:set var="lbTitle" value="Master Leaderboard" />
<c:set var="lbScope" value="MASTER" />
<c:set var="lbOverallHref" value="${pageContext.request.contextPath}/leaderboards" />
<%-- No single "my league" exists, so this points at Leagues to pick one. --%>
<c:set var="lbLeagueHref" value="${pageContext.request.contextPath}/leagues" />

<%@ include file="/WEB-INF/jspf/leaderboard-view.jspf" %>

</body>
</html>
