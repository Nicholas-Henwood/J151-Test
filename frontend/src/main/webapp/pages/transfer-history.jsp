<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<%@ taglib prefix="t" tagdir="/WEB-INF/tags" %>

<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Transfer History - Fantasy TryTons</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/theme.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/sidebar.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/catalog.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/transfer-history.css">
</head>
<body class="catalog-page th-page">

<c:set var="activeNav" value="transfers" scope="request" />
<%@ include file="/WEB-INF/jspf/sidebar.jspf" %>

<main class="catalog-main">
    <div class="catalog-content">

        <a class="th-back" href="${pageContext.request.contextPath}/transfers">&larr; Back to transfers</a>

        <header class="catalog-header">
            <div>
                <p class="catalog-eyebrow">Team management</p>
                <h1 class="brand-font">Transfer History</h1>
                <p class="th-intro">Every player change you've made &mdash; values, penalties, and confirmation status.</p>
            </div>
        </header>

        <c:if test="${not empty error}">
            <p class="th-alert" role="alert"><c:out value="${error}" /></p>
        </c:if>

        <c:choose>
            <c:when test="${empty history}">
                <div class="th-empty">
                    <span class="th-empty-icon" aria-hidden="true">
                        <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round"><path d="M17 3l4 4-4 4"/><path d="M21 7H8"/><path d="M7 21l-4-4 4-4"/><path d="M3 17h13"/></svg>
                    </span>
                    <h2>No transfer history yet</h2>
                    <p>No transfers have been recorded for this team yet.</p>
                </div>
            </c:when>

            <c:otherwise>
                <%-- ---------- Summary stat bar ---------- --%>
                <c:set var="net" value="${netValue}" />
                <section class="th-stats">
                    <div class="th-stat">
                        <span class="th-stat-label">Total transfers</span>
                        <span class="th-stat-value">${fn:length(history)}</span>
                    </div>
                    <div class="th-stat">
                        <span class="th-stat-label">Net value change</span>
                        <%-- Positive = net spend (red); negative = budget freed (green). --%>
                        <span class="th-stat-value ${empty net ? '' : (net.signum() > 0 ? 'is-red' : (net.signum() < 0 ? 'is-green' : ''))}">
                            <c:choose>
                                <c:when test="${empty net or net.signum() == 0}"><t:money value="${empty net ? 0 : net}" /></c:when>
                                <c:when test="${net.signum() > 0}">+<t:money value="${net}" /></c:when>
                                <c:otherwise>&minus;<t:money value="${net.negate()}" /></c:otherwise>
                            </c:choose>
                        </span>
                    </div>
                    <div class="th-stat">
                        <span class="th-stat-label">Penalty points</span>
                        <span class="th-stat-value ${penaltyTotal > 0 ? 'is-red' : ''}">${empty penaltyTotal ? 0 : penaltyTotal}</span>
                    </div>
                    <div class="th-stat">
                        <span class="th-stat-label">Confirmed</span>
                        <span class="th-stat-value is-gold">${empty confirmedCount ? 0 : confirmedCount}</span>
                    </div>
                </section>

                <%-- ---------- Timeline ---------- --%>
                <div class="th-timeline">
                    <c:forEach var="transfer" items="${history}">
                        <c:set var="outName" value="${empty transfer.removedPlayerName ? 'Unknown player' : transfer.removedPlayerName}" />
                        <c:set var="inName" value="${empty transfer.addedPlayerName ? 'Unknown player' : transfer.addedPlayerName}" />
                        <c:set var="outParts" value="${fn:split(outName, ' ')}" />
                        <c:set var="inParts" value="${fn:split(inName, ' ')}" />
                        <c:set var="diff" value="${transfer.valueDifference}" />
                        <c:set var="st" value="${empty transfer.status ? 'UNKNOWN' : fn:toUpperCase(transfer.status)}" />
                        <c:set var="stClass" value="${st == 'CONFIRMED' ? 'swap-status-confirmed'
                                                   : (st == 'PENDING' ? 'swap-status-pending'
                                                   : (st == 'UNKNOWN' ? 'swap-status-pending' : 'swap-status-reverted'))}" />

                        <div class="th-entry">
                            <div class="th-when">${empty historyDateLabels[transfer.transferId] ? 'Not recorded' : historyDateLabels[transfer.transferId]}</div>
                            <span class="th-node" aria-hidden="true"></span>

                            <div class="swap">
                                <div class="swap-row">
                                    <div class="swap-side">
                                        <span class="swap-avatar swap-avatar-out" aria-hidden="true"><c:if test="${fn:length(outParts) > 0}">${fn:toUpperCase(fn:substring(outParts[0], 0, 1))}<c:if test="${fn:length(outParts) > 1}">${fn:toUpperCase(fn:substring(outParts[fn:length(outParts) - 1], 0, 1))}</c:if></c:if></span>
                                        <span class="swap-info">
                                            <span class="swap-tag swap-tag-out">Out</span>
                                            <p class="swap-name" title="${fn:escapeXml(outName)}">${fn:escapeXml(outName)}</p>
                                            <p class="swap-value"><c:choose><c:when test="${empty transfer.removedPlayerValue}">&ndash;</c:when><c:otherwise><t:money value="${transfer.removedPlayerValue}" /></c:otherwise></c:choose></p>
                                        </span>
                                    </div>

                                    <span class="swap-arrow" aria-hidden="true">
                                        <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M17 3l4 4-4 4"/><path d="M21 7H8"/><path d="M7 21l-4-4 4-4"/><path d="M3 17h13"/></svg>
                                    </span>

                                    <div class="swap-side">
                                        <span class="swap-avatar swap-avatar-in" aria-hidden="true"><c:if test="${fn:length(inParts) > 0}">${fn:toUpperCase(fn:substring(inParts[0], 0, 1))}<c:if test="${fn:length(inParts) > 1}">${fn:toUpperCase(fn:substring(inParts[fn:length(inParts) - 1], 0, 1))}</c:if></c:if></span>
                                        <span class="swap-info">
                                            <span class="swap-tag swap-tag-in">In</span>
                                            <p class="swap-name" title="${fn:escapeXml(inName)}">${fn:escapeXml(inName)}</p>
                                            <p class="swap-value"><c:choose><c:when test="${empty transfer.addedPlayerValue}">&ndash;</c:when><c:otherwise><t:money value="${transfer.addedPlayerValue}" /></c:otherwise></c:choose></p>
                                        </span>
                                    </div>
                                </div>

                                <div class="swap-meta">
                                    <span class="swap-meta-item">
                                        <span class="swap-meta-label">Value diff</span>
                                        <span class="swap-diff ${empty diff ? 'is-zero' : (diff.signum() > 0 ? 'is-red' : (diff.signum() < 0 ? 'is-green' : 'is-zero'))}">
                                            <c:choose>
                                                <c:when test="${empty diff}">&ndash;</c:when>
                                                <c:when test="${diff.signum() > 0}">+<t:money value="${diff}" /></c:when>
                                                <c:when test="${diff.signum() < 0}">&minus;<t:money value="${diff.negate()}" /></c:when>
                                                <c:otherwise><t:money value="${diff}" /></c:otherwise>
                                            </c:choose>
                                        </span>
                                    </span>

                                    <span class="swap-meta-item">
                                        <c:choose>
                                            <c:when test="${transfer.penaltyPoints > 0}"><span class="swap-penalty-red">&minus;${transfer.penaltyPoints} pts</span></c:when>
                                            <c:otherwise><span class="swap-penalty-none">No penalty</span></c:otherwise>
                                        </c:choose>
                                    </span>

                                    <span class="swap-status ${stClass}">${empty transfer.status ? 'Unknown' : fn:escapeXml(fn:substring(st, 0, 1))}${empty transfer.status ? '' : fn:toLowerCase(fn:substring(st, 1, fn:length(st)))}</span>
                                </div>
                            </div>
                        </div>
                    </c:forEach>
                </div>
            </c:otherwise>
        </c:choose>

    </div>
</main>
</body>
</html>
