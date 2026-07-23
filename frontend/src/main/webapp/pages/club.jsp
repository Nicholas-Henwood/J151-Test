<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<%@ taglib prefix="t" tagdir="/WEB-INF/tags" %>

<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>${fn:escapeXml(club.clubName)} - Fantasy TryTons</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/theme.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/sidebar.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/catalog.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/club.css">
</head>
<body class="catalog-page cl-page">

<c:set var="activeNav" value="clubs" scope="request" />
<%@ include file="/WEB-INF/jspf/sidebar.jspf" %>

<main class="catalog-main">
    <div class="catalog-content">

        <a class="cl-back" href="${pageContext.request.contextPath}/clubs?submit=clubs">&larr; Back to clubs</a>

        <c:choose>
            <c:when test="${empty club}">
                <p class="cl-alert" role="alert">${empty error ? 'This club could not be loaded.' : fn:escapeXml(error)}</p>
            </c:when>

            <c:otherwise>
                <c:set var="clubName" value="${empty club.clubName ? '' : club.clubName}" />
                <c:set var="crestParts" value="${fn:split(clubName, ' ')}" />

                <%-- ---------- Hero ---------- --%>
                <section class="cl-hero">
                    <div class="cl-hero-inner">
                        <span class="cl-crest" aria-hidden="true"><c:if test="${fn:length(crestParts) > 0}">${fn:toUpperCase(fn:substring(crestParts[0], 0, 1))}<c:if test="${fn:length(crestParts) > 1}">${fn:toUpperCase(fn:substring(crestParts[fn:length(crestParts) - 1], 0, 1))}</c:if></c:if></span>

                        <div class="cl-identity">
                            <p class="cl-eyebrow">Club profile</p>
                            <div class="cl-name-row">
                                <h1 class="cl-name">${fn:escapeXml(clubName)}</h1>
                                <%-- club.active is a primitive boolean, so .active resolves. --%>
                                <span class="cl-status ${club.active ? 'cl-status-active' : 'cl-status-inactive'}">${club.active ? 'Active' : 'Inactive'}</span>
                            </div>

                            <div class="cl-meta">
                                <span class="cl-meta-item">
                                    <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true"><path d="M12 21s-7-6-7-11a7 7 0 0 1 14 0c0 5-7 11-7 11z"/><circle cx="12" cy="10" r="2.5"/></svg>
                                    ${empty club.location ? 'Not supplied' : fn:escapeXml(club.location)}
                                </span>
                                <span class="cl-meta-item">
                                    <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true"><path d="M3 21h18"/><path d="M5 21V7l7-4 7 4v14"/><path d="M10 21v-5h4v5"/></svg>
                                    ${empty club.homeVenue ? 'Not supplied' : fn:escapeXml(club.homeVenue)}
                                </span>
                            </div>
                        </div>
                    </div>

                    <%-- ---------- Derived stat strip ---------- --%>
                    <div class="cl-stats">
                        <div class="cl-stat">
                            <span class="cl-stat-label">Squad size</span>
                            <span class="cl-stat-value">${empty squadSize ? 0 : squadSize}</span>
                        </div>
                        <div class="cl-stat">
                            <span class="cl-stat-label">Total value</span>
                            <span class="cl-stat-value is-gold"><t:money value="${empty totalValue ? 0 : totalValue}" /></span>
                        </div>
                        <div class="cl-stat">
                            <span class="cl-stat-label">Avg form</span>
                            <%-- avgForm is the 0-100 mean; the rating tag shows it on the 0-10 scale. --%>
                            <span class="cl-stat-value"><t:rating value="${empty avgForm ? 0 : avgForm}" /></span>
                        </div>
                        <div class="cl-stat">
                            <span class="cl-stat-label">Best form</span>
                            <span class="cl-stat-value" title="${fn:escapeXml(bestFormName)}">${fn:escapeXml(bestFormName)}</span>
                        </div>
                    </div>
                </section>

                <%-- ---------- Roster ---------- --%>
                <div class="cl-roster-head">
                    <h2 class="cl-roster-title">Squad</h2>
                    <span class="cl-roster-rule"></span>
                    <span class="cl-roster-count">${empty squadSize ? 0 : squadSize} players from ${fn:escapeXml(clubName)}</span>
                </div>

                <c:choose>
                    <c:when test="${empty roster}">
                        <p class="cl-empty">No players are registered to this club yet.</p>
                    </c:when>

                    <c:otherwise>
                        <div class="ctable">
                            <div class="crow chead">
                                <span>Player</span>
                                <span>Position</span>
                                <span>Value</span>
                                <span>Form</span>
                            </div>
                            <div class="cbody">
                                <c:forEach var="player" items="${roster}">
                                    <c:set var="playerName" value="${empty player.playerName ? '' : player.playerName}" />
                                    <c:set var="nameParts" value="${fn:split(playerName, ' ')}" />
                                    <c:set var="positionName" value="${positionNamesById[player.positionId]}" />
                                    <c:set var="isForward" value="${positionCategoriesById[player.positionId] == 'FORWARD'}" />
                                    <c:set var="formScore" value="${player.currentForm / 10}" />

                                    <div class="crow">
                                        <span class="c-name">
                                            <span class="c-avatar" aria-hidden="true"><c:if test="${fn:length(nameParts) > 0}">${fn:toUpperCase(fn:substring(nameParts[0], 0, 1))}<c:if test="${fn:length(nameParts) > 1}">${fn:toUpperCase(fn:substring(nameParts[fn:length(nameParts) - 1], 0, 1))}</c:if></c:if></span>
                                            <span class="c-name-text" title="${fn:escapeXml(playerName)}">${fn:escapeXml(playerName)}</span>
                                        </span>

                                        <span>
                                            <span class="pos-pill ${isForward ? 'pos-fwd' : 'pos-back'}">${fn:escapeXml(positionName)}</span>
                                        </span>

                                        <span class="p-value"><t:money value="${player.value}" /></span>

                                        <span>
                                            <c:choose>
                                                <c:when test="${formScore >= 7}">
                                                    <span class="form-chip form-up"><span aria-hidden="true">&uarr;</span><t:rating value="${player.currentForm}" /></span>
                                                </c:when>
                                                <c:when test="${formScore < 5}">
                                                    <span class="form-chip form-down"><span aria-hidden="true">&darr;</span><t:rating value="${player.currentForm}" /></span>
                                                </c:when>
                                                <c:otherwise>
                                                    <span class="form-chip"><span aria-hidden="true">&ndash;</span><t:rating value="${player.currentForm}" /></span>
                                                </c:otherwise>
                                            </c:choose>
                                        </span>
                                    </div>
                                </c:forEach>
                            </div>
                        </div>
                    </c:otherwise>
                </c:choose>
            </c:otherwise>
        </c:choose>

    </div>
</main>
</body>
</html>
