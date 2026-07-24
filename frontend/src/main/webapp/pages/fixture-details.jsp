<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>

<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Fixture Details - Fantasy TryTons</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/theme.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/sidebar.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/catalog.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/fixture-details.css">
</head>
<body class="catalog-page fxd-page">

<c:set var="activeNav" value="fixtures" scope="request" />
<%@ include file="/WEB-INF/jspf/sidebar.jspf" %>

<main class="catalog-main">
    <div class="catalog-content">

        <a class="fxd-back" href="${pageContext.request.contextPath}/fixtures?submit=fixtures">&larr; Back to fixtures</a>

        <c:if test="${not empty error}">
            <p class="fxd-alert" role="alert"><c:out value="${error}" /></p>
        </c:if>

        <c:if test="${not empty fixture}">

            <c:set var="status" value="${empty fixture.fixtureStatus ? 'UPCOMING' : fixture.fixtureStatus}" />
            <c:set var="aParts" value="${fn:split(fixture.teamAName, ' ')}" />
            <c:set var="bParts" value="${fn:split(fixture.teamBName, ' ')}" />
            <%-- Winner is derived from the scores rather than trusting a side label. --%>
            <c:set var="scored" value="${not empty matchResult}" />
            <c:set var="aWon" value="${scored and not matchResult.draw and matchResult.teamAScore > matchResult.teamBScore}" />
            <c:set var="bWon" value="${scored and not matchResult.draw and matchResult.teamBScore > matchResult.teamAScore}" />

            <%-- =================== Scoreboard hero =================== --%>
            <section class="fxd-hero">
                <div class="fxd-hero-bg" aria-hidden="true"></div>
                <div class="fxd-hero-overlay" aria-hidden="true"></div>
                <div class="fxd-hero-inner">
                    <div class="fxd-hero-top">
                        <p class="fxd-hero-eyebrow">${fixture.fixtureDate}<c:if test="${not empty fixture.fixtureTime}"> &middot; ${fn:substring(fixture.fixtureTime, 0, 5)}</c:if></p>
                        <c:choose>
                            <c:when test="${status == 'COMPLETED'}">
                                <span class="fxd-pill fxd-pill-ft"><span class="fxd-pill-dot"></span>Full time</span>
                            </c:when>
                            <c:otherwise>
                                <span class="fxd-pill fxd-pill-live"><span class="fxd-pill-dot"></span>${fn:substring(status, 0, 1)}${fn:toLowerCase(fn:substring(status, 1, fn:length(status)))}</span>
                            </c:otherwise>
                        </c:choose>
                    </div>

                    <div class="fxd-matchup">
                        <div class="fxd-side fxd-side-a">
                            <p class="fxd-team-name" title="${fn:escapeXml(fixture.teamAName)}">${fn:escapeXml(fixture.teamAName)}</p>
                            <c:if test="${aWon}"><span class="fxd-winner">&#9733; Winner</span></c:if>
                        </div>
                        <span class="fxd-crest" aria-hidden="true"><c:if test="${fn:length(aParts) > 0}">${fn:toUpperCase(fn:substring(aParts[0], 0, 1))}<c:if test="${fn:length(aParts) > 1}">${fn:toUpperCase(fn:substring(aParts[fn:length(aParts) - 1], 0, 1))}</c:if></c:if></span>

                        <div class="fxd-score">
                            <c:choose>
                                <c:when test="${scored}">
                                    <span class="${aWon ? 'is-win' : 'is-loss'}">${matchResult.teamAScore}</span><span class="fxd-score-dash">&ndash;</span><span class="${bWon ? 'is-win' : 'is-loss'}">${matchResult.teamBScore}</span>
                                </c:when>
                                <c:otherwise>
                                    <span class="fxd-kickoff">${empty fixture.fixtureTime ? 'TBC' : fn:substring(fixture.fixtureTime, 0, 5)}</span>
                                </c:otherwise>
                            </c:choose>
                        </div>

                        <span class="fxd-crest" aria-hidden="true"><c:if test="${fn:length(bParts) > 0}">${fn:toUpperCase(fn:substring(bParts[0], 0, 1))}<c:if test="${fn:length(bParts) > 1}">${fn:toUpperCase(fn:substring(bParts[fn:length(bParts) - 1], 0, 1))}</c:if></c:if></span>
                        <div class="fxd-side fxd-side-b">
                            <p class="fxd-team-name" title="${fn:escapeXml(fixture.teamBName)}">${fn:escapeXml(fixture.teamBName)}</p>
                            <c:if test="${bWon}"><span class="fxd-winner">&#9733; Winner</span></c:if>
                        </div>
                    </div>

                    <c:if test="${scored}">
                        <p class="fxd-result-caption">
                            <c:choose>
                                <c:when test="${matchResult.draw}">Match drawn</c:when>
                                <c:otherwise>${fn:escapeXml(aWon ? fixture.teamAName : fixture.teamBName)} won</c:otherwise>
                            </c:choose>
                            <c:if test="${status == 'COMPLETED' and not empty fixture.simulationDate}"> &middot; Simulated ${fn:substring(fn:replace(fixture.simulationDate, 'T', ' '), 0, 16)}</c:if>
                        </p>
                    </c:if>
                </div>
            </section>

            <%-- =================== Team-side score cards =================== --%>
            <c:if test="${not empty teamScores}">
                <div class="fxd-cards">
                    <c:forEach var="ts" items="${teamScores}">
                        <c:set var="tsIsA" value="${ts.teamId == fixture.teamAId}" />
                        <c:set var="tsName" value="${tsIsA ? fixture.teamAName : fixture.teamBName}" />
                        <c:set var="tsParts" value="${fn:split(tsName, ' ')}" />
                        <c:set var="tsWon" value="${(tsIsA and aWon) or (not tsIsA and bWon)}" />
                        <section class="panel fxd-card">
                            <div class="fxd-card-head">
                                <span class="fxd-crest fxd-crest-sm" aria-hidden="true"><c:if test="${fn:length(tsParts) > 0}">${fn:toUpperCase(fn:substring(tsParts[0], 0, 1))}<c:if test="${fn:length(tsParts) > 1}">${fn:toUpperCase(fn:substring(tsParts[fn:length(tsParts) - 1], 0, 1))}</c:if></c:if></span>
                                <span class="fxd-card-name">${fn:escapeXml(tsName)}</span>
                                <c:if test="${tsWon}"><span class="fxd-winner">&#9733; Winner</span></c:if>
                            </div>
                            <div class="fxd-card-lines">
                                <div class="fxd-line"><span>Player points</span><span class="fxd-line-val">${ts.playerPoints}</span></div>
                                <div class="fxd-line"><span>Captain bonus</span><span class="fxd-line-val">+${ts.captainBonus}</span></div>
                                <div class="fxd-line"><span>Transfer penalty</span><span class="fxd-line-val ${ts.transferPenalty > 0 ? 'is-neg' : ''}">${ts.transferPenalty > 0 ? '&minus;' : ''}${ts.transferPenalty}</span></div>
                                <div class="fxd-line fxd-line-total"><span>Total score</span><span class="fxd-line-val">${ts.totalScore}</span></div>
                            </div>
                            <p class="fxd-card-foot">Calculated ${fn:substring(fn:replace(ts.calculatedAt, 'T', ' '), 0, 16)}</p>
                        </section>
                    </c:forEach>
                </div>
            </c:if>

            <%-- =================== Player statistics =================== --%>
            <c:if test="${not empty playerStats}">

                <%-- Default the visible side to the selected breakdown's team, so an
                     expanded row is never hidden behind the other tab. --%>
                <c:set var="defaultSide" value="A" />
                <c:if test="${not empty selectedPoints}">
                    <c:forEach var="ps" items="${playerStats}">
                        <c:if test="${ps.statId == selectedPoints.statId and ps.teamId == fixture.teamBId}">
                            <c:set var="defaultSide" value="B" />
                        </c:if>
                    </c:forEach>
                </c:if>

                <section class="fxd-stats">
                    <h2 class="fxd-stats-title">Player statistics</h2>

                    <%-- CSS-only segmented control: the radios drive row visibility and the
                         active-button style via :checked, so no script is needed. --%>
                    <input type="radio" name="fxdSide" id="fxdSideA" class="fxd-seg-radio" ${defaultSide == 'A' ? 'checked' : ''}>
                    <input type="radio" name="fxdSide" id="fxdSideB" class="fxd-seg-radio" ${defaultSide == 'B' ? 'checked' : ''}>
                    <div class="fxd-seg" role="tablist" aria-label="Team">
                        <label class="fxd-seg-btn" for="fxdSideA">${fn:escapeXml(fixture.teamAName)}</label>
                        <label class="fxd-seg-btn" for="fxdSideB">${fn:escapeXml(fixture.teamBName)}</label>
                    </div>

                    <div class="stbl">
                        <div class="stbl-scroll">
                            <div class="stbl-head">
                                <span class="stbl-c-player">Player</span>
                                <span>Tries</span><span>Assists</span><span>Tackles</span><span>Missed</span>
                                <span>Conv.</span><span>Pen.</span><span>Meters</span><span>Cards</span>
                                <span aria-hidden="true"></span>
                            </div>

                            <c:forEach var="sideKey" items="${['A','B']}">
                                <c:set var="sideTeamId" value="${sideKey == 'A' ? fixture.teamAId : fixture.teamBId}" />
                                <div class="stbl-body stbl-side-${fn:toLowerCase(sideKey)}">
                                    <c:forEach var="ps" items="${playerStats}">
                                        <c:if test="${ps.teamId == sideTeamId}">
                                            <c:set var="pname" value="${playerNamesById[ps.playerId]}" />
                                            <c:set var="isOpen" value="${not empty selectedPoints and ps.statId == selectedPoints.statId}" />
                                            <a class="stbl-row ${isOpen ? 'is-open' : ''}"
                                               href="${pageContext.request.contextPath}/fixture?submit=fixture&amp;fixtureId=${fixture.fixtureId}&amp;statId=${ps.statId}#stat-${ps.statId}"
                                               id="stat-${ps.statId}">
                                                <span class="stbl-c-player">
                                                    <span class="stbl-avatar" aria-hidden="true">#</span>
                                                    <span class="stbl-pname"><c:out value="${empty pname ? 'Unknown player' : pname}" /></span>
                                                </span>
                                                <span>${ps.tries}</span>
                                                <span>${ps.assists}</span>
                                                <span>${ps.tackles}</span>
                                                <span>${ps.missedTackles}</span>
                                                <span>${ps.conversions}</span>
                                                <span>${ps.penalties}</span>
                                                <span>${ps.metersGained}</span>
                                                <span class="stbl-cards">
                                                    <c:choose>
                                                        <c:when test="${ps.yellowCards > 0 or ps.redCards > 0}"><c:forEach begin="1" end="${ps.yellowCards}"><span class="stbl-card is-yellow"></span></c:forEach><c:forEach begin="1" end="${ps.redCards}"><span class="stbl-card is-red"></span></c:forEach></c:when>
                                                        <c:otherwise><span class="stbl-dash">&ndash;</span></c:otherwise>
                                                    </c:choose>
                                                </span>
                                                <span class="stbl-chev" aria-hidden="true">
                                                    <svg width="15" height="15" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.2" stroke-linecap="round" stroke-linejoin="round"><path d="M9 18l6-6-6-6"/></svg>
                                                </span>
                                            </a>

                                            <%-- Server-driven breakdown for the selected row only. --%>
                                            <c:if test="${isOpen}">
                                                <div class="stbl-breakdown">
                                                    <p class="stbl-bd-title">Points breakdown &middot; ${selectedPoints.final ? 'Final' : 'Provisional'}</p>
                                                    <c:if test="${not empty breakdowns}">
                                                        <ul class="stbl-bd-list">
                                                            <c:forEach var="bd" items="${breakdowns}">
                                                                <li class="stbl-bd-row">
                                                                    <span class="stbl-bd-cat">${fn:escapeXml(bd.category)}</span>
                                                                    <span class="stbl-bd-desc">${fn:escapeXml(bd.description)}</span>
                                                                    <span class="stbl-bd-pts ${bd.points < 0 ? 'is-neg' : ''}">${bd.points}</span>
                                                                </li>
                                                            </c:forEach>
                                                        </ul>
                                                    </c:if>
                                                    <div class="stbl-bd-total"><span>Total points</span><span>${selectedPoints.totalPoints}</span></div>
                                                </div>
                                            </c:if>
                                        </c:if>
                                    </c:forEach>
                                </div>
                            </c:forEach>
                        </div>
                    </div>
                </section>
            </c:if>

        </c:if>

    </div>
</main>

</body>
</html>
