<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<%@ taglib prefix="t" tagdir="/WEB-INF/tags" %>

<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>My Team - Fantasy TryTons</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/theme.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/sidebar.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/catalog.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/my-team.css">
</head>
<body class="catalog-page mt-page">

<c:set var="activeNav" value="my-team" scope="request" />
<%@ include file="/WEB-INF/jspf/sidebar.jspf" %>

<main class="catalog-main">
    <div class="catalog-content">

        <c:choose>
            <c:when test="${empty team}">
                <header class="catalog-header">
                    <div>
                        <p class="catalog-eyebrow">Your fantasy squad</p>
                        <h1 class="brand-font">My Team</h1>
                    </div>
                </header>
                <div class="mt-empty">
                    <h2>You do not currently have a team</h2>
                    <p>Create a team now to view your team.</p>
                    <a class="btn-gold" href="${pageContext.request.contextPath}/create-team">Create a team</a>
                </div>
            </c:when>

            <c:otherwise>
                <%-- Split counts for the "Squad" stat and bench header. --%>
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
                        <p class="catalog-eyebrow">Your fantasy squad</p>
                        <div class="mt-header-row">
                            <h1 class="brand-font">${fn:escapeXml(team.teamName)}</h1>
                            <%-- FantasyTeam booleans are wrapper Booleans, so read isValid. --%>
                            <span class="mt-valid-pill ${team.isValid ? 'mt-valid-ok' : 'mt-valid-bad'}">
                                ${team.isValid ? 'Valid squad' : 'Invalid squad'}
                            </span>
                        </div>
                        <c:if test="${not empty team.ownerUsername}">
                            <p class="mt-subline">Managed by @${fn:escapeXml(team.ownerUsername)}</p>
                        </c:if>
                    </div>
                    <div class="mt-actions">
                        <%-- No opponent teamId is available on this page, so this points at the
                             standings where opponents' teams are reachable. --%>
                        <a class="mt-ghost" href="${pageContext.request.contextPath}/leaderboard">View opponents</a>
                        <a class="btn-gold" href="${pageContext.request.contextPath}/fantasy-team/update?teamId=${team.teamId}">Update team</a>
                    </div>
                </header>

                <c:if test="${not empty error}">
                    <p class="mt-alert" role="alert"><c:out value="${error}" /></p>
                </c:if>

                <%-- ---------- Stat bar ---------- --%>
                <section class="mt-stats">
                    <div class="mt-stat">
                        <span class="mt-stat-label">Team value</span>
                        <span class="mt-stat-value is-gold"><t:money value="${team.totalTeamValue}" /></span>
                    </div>
                    <div class="mt-stat">
                        <span class="mt-stat-label">Remaining budget</span>
                        <span class="mt-stat-value"><t:money value="${team.remainingBudget}" /></span>
                    </div>
                    <div class="mt-stat">
                        <span class="mt-stat-label">Total points</span>
                        <span class="mt-stat-value">${team.totalPoints}</span>
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
                            <p>This team has no players. Build your squad to see it on the pitch.</p>
                            <a class="btn-gold" href="${pageContext.request.contextPath}/fantasy-team/update?teamId=${team.teamId}">Pick players</a>
                        </div>
                    </c:when>

                    <c:otherwise>
                        <div class="mt-body" id="myTeam">
                            <div>
                                <%-- ---------- Pitch ---------- --%>
                                <div class="mt-pitch" id="mtPitch"></div>

                                <%-- ---------- Bench ---------- --%>
                                <div class="mt-bench-head">
                                    <p class="mt-bench-title">Bench</p>
                                    <span class="mt-bench-rule"></span>
                                    <span class="mt-bench-count" id="mtBenchCount"></span>
                                </div>
                                <div class="mt-bench" id="mtBench"></div>
                            </div>

                            <%-- ---------- Detail panel (JS fills; captain by default) ---------- --%>
                            <aside class="mt-detail" id="mtDetail">
                                <p class="mt-detail-empty">Select a player to see their details.</p>
                            </aside>

                            <%-- Data rows: my-team.js reads these into the pitch, bench and panel.
                                 Ability ratings are the raw 0-100 ints the DTO carries. --%>
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
