<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>

<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Fixtures - Fantasy TryTons</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/theme.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/sidebar.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/catalog.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/fixtures.css">
</head>
<body class="catalog-page fixtures-page">

<c:set var="activeNav" value="fixtures" scope="request" />
<%@ include file="/WEB-INF/jspf/sidebar.jspf" %>

<c:set var="statusLabels" value="UPCOMING,LOCKED,SIMULATING,COMPLETED,PROCESSED,CANCELLED" />

<main class="catalog-main">
    <div class="catalog-content">

        <header class="catalog-header">
            <div>
                <p class="catalog-eyebrow">Fantasy TryTons League</p>
                <h1 class="brand-font">Fixtures</h1>
            </div>
        </header>

        <c:if test="${not empty error}">
            <p class="catalog-error" role="alert"><c:out value="${error}" /></p>
        </c:if>

        <%-- ---------- Featured fixture ---------- --%>
        <c:if test="${not empty featuredFixture}">
            <c:set var="fx" value="${featuredFixture}" />
            <c:set var="fxStatus" value="${empty fx.fixtureStatus ? 'UPCOMING' : fn:toUpperCase(fx.fixtureStatus)}" />
            <c:set var="fxPlayed" value="${fxStatus == 'COMPLETED' or fxStatus == 'PROCESSED' or fxStatus == 'CANCELLED'}" />
            <c:set var="aParts" value="${fn:split(fx.teamAName, ' ')}" />
            <c:set var="bParts" value="${fn:split(fx.teamBName, ' ')}" />

            <section class="fx-hero">
                <div class="fx-hero-inner">
                    <div class="fx-hero-top">
                        <%-- Labelled honestly: with nothing left to play, this is the most
                             recent fixture rather than the next one. --%>
                        <p class="fx-hero-eyebrow">
                            ${fxPlayed ? 'Latest fixture' : 'Your next fixture'}
                            <c:if test="${not empty roundNumbersById[fx.roundId]}">
                                &middot; Round ${roundNumbersById[fx.roundId]}
                            </c:if>
                        </p>
                        <span class="fx-pill fx-pill-${fn:toLowerCase(fxStatus)}">${fn:escapeXml(fxStatus)}</span>
                    </div>

                    <div class="fx-matchup">
                        <div class="fx-side fx-side-home">
                            <div style="min-width:0">
                                <p class="fx-team-name ${not empty myTeamId and myTeamId == fx.teamAId ? 'is-mine' : ''}"
                                   title="${fn:escapeXml(fx.teamAName)}">${fn:escapeXml(fx.teamAName)}</p>
                                <span class="fx-team-side">Home</span>
                            </div>
                            <span class="fx-crest" aria-hidden="true"><c:if test="${fn:length(aParts) > 0}">${fn:toUpperCase(fn:substring(aParts[0], 0, 1))}<c:if test="${fn:length(aParts) > 1}">${fn:toUpperCase(fn:substring(aParts[fn:length(aParts) - 1], 0, 1))}</c:if></c:if></span>
                        </div>

                        <div class="fx-kick">
                            <span class="fx-kick-label">Kick-off</span>
                            <span class="fx-kick-time">
                                <c:choose>
                                    <c:when test="${not empty fixtureTimeById[fx.fixtureId]}">${fixtureTimeById[fx.fixtureId]}</c:when>
                                    <c:otherwise>&ndash;</c:otherwise>
                                </c:choose>
                            </span>
                            <span class="fx-kick-date">
                                ${fixtureDateById[fx.fixtureId]}
                            </span>
                        </div>

                        <div class="fx-side fx-side-away">
                            <span class="fx-crest" aria-hidden="true"><c:if test="${fn:length(bParts) > 0}">${fn:toUpperCase(fn:substring(bParts[0], 0, 1))}<c:if test="${fn:length(bParts) > 1}">${fn:toUpperCase(fn:substring(bParts[fn:length(bParts) - 1], 0, 1))}</c:if></c:if></span>
                            <div style="min-width:0">
                                <p class="fx-team-name ${not empty myTeamId and myTeamId == fx.teamBId ? 'is-mine' : ''}"
                                   title="${fn:escapeXml(fx.teamBName)}">${fn:escapeXml(fx.teamBName)}</p>
                                <span class="fx-team-side">Away</span>
                            </div>
                        </div>
                    </div>

                    <a class="btn-gold fx-hero-cta"
                       href="${pageContext.request.contextPath}/fixture?submit=fixture&amp;fixtureId=${fx.fixtureId}">
                        ${fxPlayed ? 'View match result' : 'View match preview'} &rarr;
                    </a>
                </div>
            </section>
        </c:if>

        <%-- ---------- Status filter pills ---------- --%>
        <%-- PROCESSED is in the fixture status enum but was missing from the spec's
             filter list; it is included so those fixtures stay reachable. --%>
        <div class="fx-tabs" role="group" aria-label="Filter fixtures by status">
            <c:set var="active" value="${empty statusFilter ? '' : fn:toUpperCase(statusFilter)}" />
            <button type="button" class="tab ${active == '' ? 'is-active' : ''}" data-status-tab="" aria-pressed="${active == ''}">All</button>
            <c:forEach var="status" items="${fn:split(statusLabels, ',')}">
                <button type="button" class="tab ${active == status ? 'is-active' : ''}"
                        data-status-tab="${status}" aria-pressed="${active == status}">
                    ${fn:substring(status, 0, 1)}${fn:toLowerCase(fn:substring(status, 1, fn:length(status)))}
                </button>
            </c:forEach>
        </div>

        <%-- ---------- Round groups ---------- --%>
        <c:forEach var="group" items="${fixtureGroups}">
            <section class="fx-group" data-round-group>
                <div class="fx-group-head">
                    <h2 class="fx-group-title">${fn:escapeXml(group.key)}</h2>
                    <span class="fx-group-rule"></span>
                    <c:if test="${not empty group.value[0].fixtureDate}">
                        <span class="fx-group-date">${fixtureDateById[group.value[0].fixtureId]}</span>
                    </c:if>
                </div>

                <div class="fx-list">
                    <c:forEach var="fixture" items="${group.value}">
                        <c:set var="status" value="${empty fixture.fixtureStatus ? 'UPCOMING' : fn:toUpperCase(fixture.fixtureStatus)}" />
                        <c:set var="result" value="${scoresByFixtureId[fixture.fixtureId]}" />
                        <c:set var="homeParts" value="${fn:split(fixture.teamAName, ' ')}" />
                        <c:set var="awayParts" value="${fn:split(fixture.teamBName, ' ')}" />

                        <article class="fx-card fx-card-${fn:toLowerCase(status)}" data-fixture-status="${status}">
                            <a class="fx-side fx-side-home fx-side-link"
                               href="${pageContext.request.contextPath}/fantasy-team/opponent?teamId=${fixture.teamAId}&amp;fixtureId=${fixture.fixtureId}"
                               title="View ${fn:escapeXml(fixture.teamAName)}">
                                <p class="fx-team-name ${not empty myTeamId and myTeamId == fixture.teamAId ? 'is-mine' : ''}">${fn:escapeXml(fixture.teamAName)}</p>
                                <span class="fx-crest" aria-hidden="true"><c:if test="${fn:length(homeParts) > 0}">${fn:toUpperCase(fn:substring(homeParts[0], 0, 1))}<c:if test="${fn:length(homeParts) > 1}">${fn:toUpperCase(fn:substring(homeParts[fn:length(homeParts) - 1], 0, 1))}</c:if></c:if></span>
                            </a>

                            <div class="fx-centre">
                                <c:choose>
                                    <%-- Scores come from the per-fixture match result; the list
                                         endpoint does not carry them. --%>
                                    <c:when test="${not empty result}">
                                        <span class="fx-score"><span class="${result.draw ? '' : (result.teamAScore > result.teamBScore ? 'is-winner' : 'is-loser')}">${result.teamAScore}</span>&ndash;<span class="${result.draw ? '' : (result.teamBScore > result.teamAScore ? 'is-winner' : 'is-loser')}">${result.teamBScore}</span></span>
                                    </c:when>
                                    <c:when test="${status == 'CANCELLED'}">
                                        <span class="fx-centre-dash">&mdash;</span>
                                    </c:when>
                                    <c:otherwise>
                                        <span class="fx-vs">VS</span>
                                        <c:if test="${not empty fixtureTimeById[fixture.fixtureId]}">
                                            <span class="fx-centre-time">${fixtureTimeById[fixture.fixtureId]}</span>
                                        </c:if>
                                    </c:otherwise>
                                </c:choose>
                            </div>

                            <a class="fx-side fx-side-away fx-side-link"
                               href="${pageContext.request.contextPath}/fantasy-team/opponent?teamId=${fixture.teamBId}&amp;fixtureId=${fixture.fixtureId}"
                               title="View ${fn:escapeXml(fixture.teamBName)}">
                                <span class="fx-crest" aria-hidden="true"><c:if test="${fn:length(awayParts) > 0}">${fn:toUpperCase(fn:substring(awayParts[0], 0, 1))}<c:if test="${fn:length(awayParts) > 1}">${fn:toUpperCase(fn:substring(awayParts[fn:length(awayParts) - 1], 0, 1))}</c:if></c:if></span>
                                <p class="fx-team-name ${not empty myTeamId and myTeamId == fixture.teamBId ? 'is-mine' : ''}">${fn:escapeXml(fixture.teamBName)}</p>
                            </a>

                            <div class="fx-meta">
                                <span class="fx-pill fx-pill-${fn:toLowerCase(status)}">${fn:escapeXml(status)}</span>
                                <span class="fx-meta-when">
                                    ${fixtureDateById[fixture.fixtureId]}
                                    <c:if test="${not empty fixtureTimeById[fixture.fixtureId]}">&middot; ${fixtureTimeById[fixture.fixtureId]}</c:if>
                                </span>
                                <a class="fx-view" href="${pageContext.request.contextPath}/fixture?submit=fixture&amp;fixtureId=${fixture.fixtureId}">View &rarr;</a>
                            </div>
                        </article>
                    </c:forEach>
                </div>
            </section>
        </c:forEach>

        <p class="catalog-empty" id="fixturesEmptyState" ${empty fixtures ? '' : 'hidden'}>No fixtures found for this filter.</p>

    </div>
</main>

<script src="${pageContext.request.contextPath}/assets/js/fixtures.js"></script>
</body>
</html>
