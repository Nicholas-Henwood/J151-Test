<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>

<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Points History - Fantasy TryTons</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/theme.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/sidebar.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/catalog.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/history.css">
</head>
<body class="catalog-page history-page">

<c:set var="activeNav" value="history" scope="request" />
<%@ include file="/WEB-INF/jspf/sidebar.jspf" %>

<main class="catalog-main">
    <div class="catalog-content">

        <header class="catalog-header">
            <div>
                <p class="catalog-eyebrow">Your season</p>
                <h1 class="brand-font">Points History</h1>
                <p class="hs-intro">Track your fantasy team's total points, current ranking, and how each round played out.</p>
            </div>
        </header>

        <c:if test="${not empty pointsHistoryError}">
            <p class="hs-alert" role="alert"><c:out value="${pointsHistoryError}" /></p>
        </c:if>
        <c:if test="${not empty weeklyPerformanceError}">
            <p class="hs-alert" role="alert"><c:out value="${weeklyPerformanceError}" /></p>
        </c:if>

        <c:choose>
            <c:when test="${empty pointsHistory}">
                <div class="hs-empty" style="margin-top:24px">
                    <h2>No Points Yet</h2>
                    <p>Your points history will appear here once your team has played a round.</p>
                </div>
            </c:when>

            <c:otherwise>
                <%-- ---------- Summary cards ---------- --%>
                <section class="hs-stats">
                    <div class="stat-card stat-card-accent">
                        <span class="stat-label">Total points</span>
                        <span class="stat-value">${pointsHistory.totals}</span>
                    </div>
                    <div class="stat-card">
                        <span class="stat-label">Current rank</span>
                        <c:choose>
                            <c:when test="${not empty pointsHistory.ranking}">
                                <span class="stat-value">#${pointsHistory.ranking}</span>
                            </c:when>
                            <c:otherwise>
                                <span class="stat-value is-muted">Not available</span>
                            </c:otherwise>
                        </c:choose>
                    </div>
                    <div class="stat-card">
                        <span class="stat-label">Record (W&ndash;D&ndash;L)</span>
                        <span class="stat-value">${wins}&ndash;${draws}&ndash;${losses}</span>
                    </div>
                    <div class="stat-card">
                        <span class="stat-label">Avg / round</span>
                        <span class="stat-value">${averagePerRound}</span>
                    </div>
                    <div class="stat-card">
                        <span class="stat-label">Best round</span>
                        <span class="stat-value">${bestRound}</span>
                    </div>
                </section>

                <c:choose>
                    <c:when test="${empty weeklyPerformance}">
                        <div class="hs-empty">
                            <h2>No Rounds Played Yet</h2>
                            <p>Once your team plays a fixture, each round's points and result will show here.</p>
                        </div>
                    </c:when>

                    <c:otherwise>
                        <%-- ---------- Trend chart ---------- --%>
                        <section class="panel">
                            <div class="panel-header">
                                <h2 class="panel-heading">Points per round</h2>
                                <div class="hs-legend">
                                    <span><i class="hs-swatch-win"></i>Win</span>
                                    <span><i class="hs-swatch-draw"></i>Draw</span>
                                    <span><i class="hs-swatch-loss"></i>Loss</span>
                                </div>
                            </div>

                            <div class="hs-chart">
                                <c:forEach var="round" items="${weeklyPerformance}" varStatus="rowStatus">
                                    <c:set var="result" value="${empty round.result ? '' : fn:toLowerCase(round.result)}" />
                                    <c:set var="roundNumber" value="${roundNumbersById[round.roundId]}" />
                                    <c:set var="roundLabel" value="${empty roundNumber ? rowStatus.index + 1 : roundNumber}" />
                                    <%-- Bars are scaled against the best round, so the strongest
                                         week is always full height. Guard against a zero best. --%>
                                    <c:set var="barHeight" value="${bestRound > 0 ? (round.pointsScored * 100) / bestRound : 0}" />

                                    <div class="hs-col" title="Round ${roundLabel}: ${round.pointsScored} pts &middot; ${fn:escapeXml(round.result)}">
                                        <span class="hs-col-value">${round.pointsScored}</span>
                                        <span class="hs-barwrap"><span class="hs-bar hs-bar-${result}" style="height:${barHeight}%"></span></span>
                                        <span class="hs-col-label">R${roundLabel}</span>
                                    </div>
                                </c:forEach>
                            </div>
                        </section>

                        <%-- ---------- Round-by-round breakdown ---------- --%>
                        <section class="panel">
                            <div class="panel-header">
                                <h2 class="panel-heading">Round by round</h2>
                            </div>

                            <div class="hs-table">
                                <div class="hs-thead">
                                    <span>Round</span>
                                    <span>Fixture</span>
                                    <span>Points</span>
                                    <span>Result</span>
                                </div>

                                <c:forEach var="round" items="${weeklyPerformance}" varStatus="rowStatus">
                                    <c:set var="result" value="${empty round.result ? '' : fn:toLowerCase(round.result)}" />
                                    <c:set var="roundNumber" value="${roundNumbersById[round.roundId]}" />
                                    <c:set var="roundLabel" value="${empty roundNumber ? rowStatus.index + 1 : roundNumber}" />
                                    <%-- Falls back to the raw id when the fixture is not in the
                                         list, rather than rendering an empty cell. --%>
                                    <c:set var="fixtureLabel" value="${empty fixtureLabelsById[round.fixtureId] ? round.fixtureId : fixtureLabelsById[round.fixtureId]}" />

                                    <div class="hrow">
                                        <span class="hs-cell-round"><span class="round-chip">R${roundLabel}</span></span>
                                        <span class="hs-fixture">
                                            <c:choose>
                                                <c:when test="${not empty round.fixtureId}">
                                                    <a class="hs-fixture-link"
                                                       href="${pageContext.request.contextPath}/fixture?submit=fixture&amp;fixtureId=${round.fixtureId}"
                                                       title="${fn:escapeXml(fixtureLabel)}">${fn:escapeXml(fixtureLabel)}</a>
                                                </c:when>
                                                <c:otherwise>&mdash;</c:otherwise>
                                            </c:choose>
                                        </span>
                                        <span class="hs-points">${round.pointsScored}</span>
                                        <span class="hs-result">
                                            <span class="result-badge result-${result}">${fn:escapeXml(round.result)}</span>
                                        </span>
                                    </div>
                                </c:forEach>
                            </div>
                        </section>
                    </c:otherwise>
                </c:choose>
            </c:otherwise>
        </c:choose>

    </div>
</main>
</body>
</html>
