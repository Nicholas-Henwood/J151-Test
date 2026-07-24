<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<%@ taglib prefix="t" tagdir="/WEB-INF/tags" %>

<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>${empty team ? 'Opponent' : fn:escapeXml(team.teamName)} - Fantasy TryTons</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/theme.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/sidebar.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/catalog.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/my-team.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/opponent-team.css">
</head>
<body class="catalog-page opp-page">

<c:set var="activeNav" value="fixtures" scope="request" />
<%@ include file="/WEB-INF/jspf/sidebar.jspf" %>

<main class="catalog-main">
    <div class="catalog-content">

        <%-- Back to the fixture we came from, if one was passed; else to the fixtures list. --%>
        <c:choose>
            <c:when test="${not empty param.fixtureId}">
                <a class="opp-back" href="${pageContext.request.contextPath}/fixture?submit=fixture&amp;fixtureId=${fn:escapeXml(param.fixtureId)}">&larr; Back to fixture</a>
            </c:when>
            <c:otherwise>
                <a class="opp-back" href="${pageContext.request.contextPath}/fixtures?submit=fixtures">&larr; Back to fixtures</a>
            </c:otherwise>
        </c:choose>

        <c:choose>
            <c:when test="${empty team}">
                <header class="catalog-header">
                    <div>
                        <p class="catalog-eyebrow">Opponent scouting</p>
                        <h1 class="brand-font">Opponent Team</h1>
                    </div>
                </header>
                <p class="mt-alert" role="alert">${empty error ? 'This team could not be loaded.' : fn:escapeXml(error)}</p>
            </c:when>

            <c:otherwise>
                <%-- Starter / bench split for the "Squad" stat. --%>
                <c:set var="starterCount" value="${0}" />
                <c:set var="benchCount" value="${0}" />
                <c:forEach var="s" items="${team.players}">
                    <c:choose>
                        <c:when test="${fn:toUpperCase(s.squadRole) == 'BENCH'}"><c:set var="benchCount" value="${benchCount + 1}" /></c:when>
                        <c:otherwise><c:set var="starterCount" value="${starterCount + 1}" /></c:otherwise>
                    </c:choose>
                </c:forEach>

                <header class="catalog-header">
                    <div>
                        <p class="catalog-eyebrow">Opponent scouting</p>
                        <h1 class="brand-font">${fn:escapeXml(team.teamName)}</h1>
                    </div>
                </header>

                <c:if test="${not empty error}">
                    <p class="mt-alert" role="alert"><c:out value="${error}" /></p>
                </c:if>

                <%-- ---------- Reduced stat bar: no budget/value/valid on the opponent DTO ---------- --%>
                <section class="mt-stats">
                    <div class="mt-stat">
                        <span class="mt-stat-label">Total points</span>
                        <span class="mt-stat-value is-gold">${team.totalPoints}</span>
                    </div>
                    <div class="mt-stat">
                        <span class="mt-stat-label">This round</span>
                        <span class="mt-stat-value">${team.weeklyPoints}</span>
                    </div>
                    <div class="mt-stat">
                        <span class="mt-stat-label">Squad</span>
                        <span class="mt-stat-value">${starterCount} + ${benchCount}</span>
                    </div>
                </section>

                <c:choose>
                    <c:when test="${empty team.players}">
                        <div class="mt-empty">
                            <h2>No players selected yet</h2>
                            <p>No players are selected for this team yet.</p>
                        </div>
                    </c:when>

                    <c:otherwise>
                        <%-- Same pitch/bench/detail structure and data rows as My Team; my-team.js
                             builds all three. The silver jerseys come from opponent-team.css. --%>
                        <div class="mt-body" id="myTeam">
                            <div>
                                <div class="mt-pitch" id="mtPitch"></div>

                                <div class="mt-bench-head">
                                    <p class="mt-bench-title">Bench</p>
                                    <span class="mt-bench-rule"></span>
                                    <span class="mt-bench-count" id="mtBenchCount"></span>
                                </div>
                                <div class="mt-bench" id="mtBench"></div>
                            </div>

                            <aside class="mt-detail" id="mtDetail">
                                <p class="mt-detail-empty">Select a player to see their details.</p>
                            </aside>

                            <c:forEach var="p" items="${team.players}">
                                <div class="mt-player" hidden
                                     data-id="${fn:escapeXml(p.playerId)}"
                                     data-name="${fn:escapeXml(p.playerName)}"
                                     data-position="${fn:escapeXml(p.positionName)}"
                                     data-club="${fn:escapeXml(p.clubName)}"
                                     data-value="<t:money value='${p.value}' />"
                                     data-role="${fn:escapeXml(p.squadRole)}"
                                     data-captain="${p.isCaptain}"
                                     data-vice="${p.isViceCaptain}"
                                     data-active="${p.isActive}"
                                     data-points="${p.totalFantasyPoints}"
                                     data-attacking="${p.attackingAbility}"
                                     data-defensive="${p.defensiveAbility}"
                                     data-kicking="${p.kickingAbility}"
                                     data-discipline="${p.discipline}"
                                     data-consistency="${p.consistency}"
                                     data-fitness="${p.fitness}"
                                     data-form="${p.currentForm}"></div>
                            </c:forEach>
                        </div>
                    </c:otherwise>
                </c:choose>
            </c:otherwise>
        </c:choose>

    </div>
</main>

<script src="${pageContext.request.contextPath}/assets/js/my-team.js"></script>
</body>
</html>
