<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<%@ taglib prefix="t" tagdir="/WEB-INF/tags" %>

<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Transfers - Fantasy TryTons</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/theme.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/sidebar.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/catalog.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/transfers.css">
</head>
<body class="catalog-page transfers-page">

<c:set var="activeNav" value="transfers" scope="request" />
<%@ include file="/WEB-INF/jspf/sidebar.jspf" %>

<%-- A round is locked if either source says so. Mirrors the server rules. --%>
<c:set var="isLocked" value="${(not empty lockStatus and lockStatus.locked)
                               or (not empty deadlineStatus and (deadlineStatus.locked or not deadlineStatus.openForTransfers))}" />
<c:set var="freeLeft" value="${empty freeTransfersLeft ? 1 : freeTransfersLeft}" />
<c:set var="budgetValue" value="${empty remainingBudget ? (empty budget ? 0 : budget) : remainingBudget}" />

<main class="catalog-main">
    <div class="catalog-content">

        <header class="catalog-header">
            <div>
                <p class="catalog-eyebrow">Fantasy TryTons League</p>
                <h1 class="brand-font">Transfers</h1>
                <p class="tf-subtext">Pick one player to remove, then one to bring in. Value and budget update as you go.</p>
            </div>
            <div class="tf-status">
                <span class="tf-pill ${isLocked ? 'tf-pill-locked' : 'tf-pill-open'}">
                    ${isLocked ? 'Transfers locked' : 'Transfers open'}
                </span>
                <c:if test="${not empty round}">
                    <p class="tf-status-line">
                        Round ${round.roundNumber}
                        <c:if test="${not empty lockDeadlineLabel}">
                            &middot; Deadline ${lockDeadlineLabel}
                        </c:if>
                    </p>
                </c:if>
                <p class="tf-status-line">${freeLeft} free transfer${freeLeft == 1 ? '' : 's'} left</p>
            </div>
        </header>

        <c:if test="${param.transferred == '1'}">
            <p class="catalog-error" role="status" style="border-left-color:#5fae7a">Transfer completed successfully.</p>
        </c:if>
        <c:forEach var="alert" items="${[error, transferError, lockError, squadError]}">
            <c:if test="${not empty alert}">
                <p class="catalog-error" role="alert"><c:out value="${alert}" /></p>
            </c:if>
        </c:forEach>

        <%-- ---------- Latest transfer ---------- --%>
        <c:if test="${not empty transfer}">
            <section class="tf-latest">
                <div>
                    <p class="tf-latest-title">Transfer confirmed</p>
                    <p class="tf-latest-sub">
                        <c:out value="${transfer.removedPlayerName}" /> out,
                        <c:out value="${transfer.addedPlayerName}" /> in
                    </p>
                </div>
                <span class="tf-latest-detail">
                    <t:money value="${transfer.valueDifference}" /> value
                    &middot; ${transfer.penaltyPoints} penalty
                    <c:if test="${not empty transfer.status}">&middot; <c:out value="${transfer.status}" /></c:if>
                </span>
            </section>
        </c:if>

        <form action="${pageContext.request.contextPath}/transfers" method="post" id="transferForm"
              data-budget="${budgetValue}"
              data-free-transfers="${freeLeft}"
              data-locked="${isLocked}">
            <input type="hidden" name="submit" value="transfer">
            <input type="hidden" name="teamId" value="${fn:escapeXml(teamId)}">
            <input type="hidden" name="roundId" value="${fn:escapeXml(roundId)}">
            <input type="hidden" name="removedPlayerId" id="removedPlayerId" value="">
            <input type="hidden" name="addedPlayerId" id="addedPlayerId" value="">

            <%-- ---------- Swap summary ---------- --%>
            <section class="tf-summary">
                <div class="tf-slots">
                    <div class="tf-slot tf-slot-out" id="slotOut">
                        <span class="tf-slot-disc">&ndash;</span>
                        <span class="tf-slot-body">
                            <span class="tf-slot-tag">Out</span>
                            <p class="tf-slot-name">Select from your squad</p>
                            <p class="tf-slot-meta"></p>
                        </span>
                    </div>

                    <span class="tf-swap-icon" aria-hidden="true">
                        <svg width="22" height="22" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M17 3l4 4-4 4"/><path d="M21 7H8"/><path d="M7 21l-4-4 4-4"/><path d="M3 17h13"/></svg>
                    </span>

                    <div class="tf-slot tf-slot-in" id="slotIn">
                        <span class="tf-slot-disc">+</span>
                        <span class="tf-slot-body">
                            <span class="tf-slot-tag">In</span>
                            <p class="tf-slot-name">Select an available player</p>
                            <p class="tf-slot-meta"></p>
                        </span>
                    </div>
                </div>

                <%-- Hidden until the swap exceeds the free-transfer allowance. The backend
                     refuses an unacknowledged penalty transfer outright. --%>
                <label class="tf-penalty" id="penaltyWrap" hidden>
                    <input type="checkbox" name="penaltyConfirmed" id="penaltyConfirmed" value="true">
                    <span>You have no free transfers left this round, so this swap may apply a
                        <strong>-4 point</strong> penalty. I understand.</span>
                </label>

                <div class="tf-summary-foot">
                    <div>
                        <span class="tf-stat-label">Value change</span>
                        <span class="tf-stat-value" id="valueChange">&mdash;</span>
                    </div>
                    <div>
                        <span class="tf-stat-label">Budget after</span>
                        <span class="tf-stat-value is-gold" id="budgetAfter"><t:money value="${budgetValue}" /></span>
                    </div>

                    <div class="tf-actions">
                        <button type="button" class="tf-ghost" id="clearSelection" hidden>Clear</button>
                        <button type="submit" class="btn-gold tf-confirm" id="confirmTransfer" disabled>Confirm transfer</button>
                    </div>

                    <p class="tf-warning" id="affordWarning" hidden>Not enough budget for this swap.</p>
                </div>
            </section>

            <%-- ---------- Picker panels ---------- --%>
            <div class="tf-panels">

                <section class="panel">
                    <div class="panel-head">
                        <div>
                            <p class="panel-title">Your squad</p>
                            <p class="panel-helper">Tap a player to remove</p>
                        </div>
                        <c:set var="squadValue" value="${0}" />
                        <c:forEach var="member" items="${squad}">
                            <c:set var="squadValue" value="${squadValue + (empty member.value ? 0 : member.value)}" />
                        </c:forEach>
                        <span class="panel-metric">Squad value <strong><t:money value="${squadValue}" /></strong></span>
                    </div>
                    <div class="panel-body">
                        <c:choose>
                            <c:when test="${empty squad}">
                                <p class="panel-empty">
                                    Your squad could not be loaded.
                                    <a class="tf-history-link" href="${pageContext.request.contextPath}/create-team">Create a team</a> to start transferring.
                                </p>
                            </c:when>
                            <c:otherwise>
                                <c:forEach var="member" items="${squad}">
                                    <c:set var="memberName" value="${empty member.playerName ? '' : member.playerName}" />
                                    <c:set var="parts" value="${fn:split(memberName, ' ')}" />
                                    <button type="button" class="pick-row" data-pick="out"
                                            data-player-id="${fn:escapeXml(member.playerId)}"
                                            data-name="${fn:escapeXml(memberName)}"
                                            data-club="${fn:escapeXml(member.clubName)}"
                                            data-value="${member.value}"
                                            data-initials="<c:if test='${fn:length(parts) > 0}'>${fn:toUpperCase(fn:substring(parts[0], 0, 1))}<c:if test='${fn:length(parts) > 1}'>${fn:toUpperCase(fn:substring(parts[fn:length(parts) - 1], 0, 1))}</c:if></c:if>">
                                        <span class="pick-radio" aria-hidden="true"></span>
                                        <span class="pick-avatar" aria-hidden="true"><c:if test="${fn:length(parts) > 0}">${fn:toUpperCase(fn:substring(parts[0], 0, 1))}<c:if test="${fn:length(parts) > 1}">${fn:toUpperCase(fn:substring(parts[fn:length(parts) - 1], 0, 1))}</c:if></c:if></span>
                                        <span class="pick-name">
                                            <span class="pick-name-text">${fn:escapeXml(memberName)}</span>
                                            <span class="pick-club">${fn:escapeXml(member.clubName)}</span>
                                        </span>
                                        <span class="pos-pill ${positionCategoryByName[member.positionName] == "FORWARD" ? "pos-fwd" : "pos-back"}">${fn:escapeXml(member.positionName)}</span>
                                        <span class="pick-value"><t:money value="${member.value}" /></span>
                                        <span class="pick-metric">${member.totalFantasyPoints} pts</span>
                                    </button>
                                </c:forEach>
                            </c:otherwise>
                        </c:choose>
                    </div>
                </section>

                <section class="panel">
                    <div class="panel-head">
                        <div>
                            <p class="panel-title">Available players</p>
                            <p class="panel-helper">Tap a player to bring in</p>
                        </div>
                        <label class="search-wrap" for="availableSearch">
                            <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true"><circle cx="11" cy="11" r="7"/><path d="M21 21l-4.3-4.3"/></svg>
                            <input type="search" id="availableSearch" placeholder="Search player"
                                   autocomplete="off" aria-label="Search available players">
                        </label>
                    </div>
                    <div class="panel-body">
                        <c:choose>
                            <c:when test="${empty players}">
                                <p class="panel-empty">No available players could be loaded right now.</p>
                            </c:when>
                            <c:otherwise>
                                <c:forEach var="option" items="${players}">
                                    <c:set var="optionName" value="${empty option.playerName ? '' : option.playerName}" />
                                    <c:set var="optionClub" value="${clubNamesById[option.clubId]}" />
                                    <c:set var="optionPosition" value="${positionNamesById[option.positionId]}" />
                                    <c:set var="parts" value="${fn:split(optionName, ' ')}" />
                                    <c:set var="formScore" value="${option.currentForm / 10}" />
                                    <button type="button" class="pick-row" data-pick="in"
                                            data-player-id="${fn:escapeXml(option.playerId)}"
                                            data-name="${fn:escapeXml(optionName)}"
                                            data-club="${fn:escapeXml(optionClub)}"
                                            data-value="${option.value}"
                                            data-search="${fn:escapeXml(fn:toLowerCase(optionName))} ${fn:escapeXml(fn:toLowerCase(optionClub))}"
                                            data-initials="<c:if test='${fn:length(parts) > 0}'>${fn:toUpperCase(fn:substring(parts[0], 0, 1))}<c:if test='${fn:length(parts) > 1}'>${fn:toUpperCase(fn:substring(parts[fn:length(parts) - 1], 0, 1))}</c:if></c:if>">
                                        <span class="pick-radio" aria-hidden="true"></span>
                                        <span class="pick-avatar" aria-hidden="true"><c:if test="${fn:length(parts) > 0}">${fn:toUpperCase(fn:substring(parts[0], 0, 1))}<c:if test="${fn:length(parts) > 1}">${fn:toUpperCase(fn:substring(parts[fn:length(parts) - 1], 0, 1))}</c:if></c:if></span>
                                        <span class="pick-name">
                                            <span class="pick-name-text">${fn:escapeXml(optionName)}</span>
                                            <span class="pick-club">${fn:escapeXml(optionClub)}</span>
                                        </span>
                                        <span class="pos-pill ${positionCategoryByName[optionPosition] == "FORWARD" ? "pos-fwd" : "pos-back"}">${fn:escapeXml(optionPosition)}</span>
                                        <span class="pick-value"><t:money value="${option.value}" /></span>
                                        <span class="pick-metric ${formScore >= 7 ? '' : 'is-dim'}">Form <t:rating value="${option.currentForm}" /></span>
                                    </button>
                                </c:forEach>
                                <p class="panel-empty" id="availableEmpty" hidden>No players match your search.</p>
                            </c:otherwise>
                        </c:choose>
                    </div>
                </section>

            </div>
        </form>

        <%-- ---------- Recommendations ---------- --%>
        <c:if test="${not empty transferRecommendations}">
            <section class="tf-recs">
                <div class="tf-recs-head">
                    <svg width="17" height="17" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true"><path d="M12 3l2.6 5.6 6 .8-4.4 4.2 1.1 6-5.3-2.9-5.3 2.9 1.1-6L3.4 9.4l6-.8z"/></svg>
                    <h2>Recommended for you</h2>
                </div>
                <p class="tf-recs-sub">Suggested swaps based on your current squad.</p>

                <div class="tf-recs-row">
                    <c:forEach var="rec" items="${transferRecommendations}" varStatus="recStatus">
                        <c:set var="recName" value="${empty rec.playerName ? '' : rec.playerName}" />
                        <c:set var="recParts" value="${fn:split(recName, ' ')}" />
                        <c:set var="recForm" value="${rec.currentForm / 10}" />
                        <%-- RecommendedPlayer carries only replacesPlayerId, so the name is
                             resolved against the squad already on the page. --%>
                        <c:set var="replacesName" value="" />
                        <c:forEach var="member" items="${squad}">
                            <c:if test="${member.playerId == rec.replacesPlayerId}">
                                <c:set var="replacesName" value="${member.playerName}" />
                            </c:if>
                        </c:forEach>

                        <article class="rec-card">
                            <div class="rec-head">
                                <span class="pick-avatar" aria-hidden="true"><c:if test="${fn:length(recParts) > 0}">${fn:toUpperCase(fn:substring(recParts[0], 0, 1))}<c:if test="${fn:length(recParts) > 1}">${fn:toUpperCase(fn:substring(recParts[fn:length(recParts) - 1], 0, 1))}</c:if></c:if></span>
                                <div style="min-width:0">
                                    <p class="rec-name">${fn:escapeXml(recName)}</p>
                                    <p class="rec-club">${fn:escapeXml(rec.clubName)}</p>
                                </div>
                                <span class="pos-pill ${positionCategoryByName[rec.positionName] == "FORWARD" ? "pos-fwd" : "pos-back"}">${fn:escapeXml(rec.positionName)}</span>
                            </div>

                            <p class="rec-line">
                                Value <strong><t:money value="${rec.value}" /></strong>
                                &middot; Form <span class="${recForm >= 7 ? 'is-strong-form' : ''}"><t:rating value="${rec.currentForm}" /></span>
                            </p>

                            <c:if test="${not empty replacesName}">
                                <p class="rec-replaces">Replaces ${fn:escapeXml(replacesName)}</p>
                            </c:if>

                            <c:if test="${not empty rec.reason}">
                                <button type="button" class="rec-why" data-rec-why="recReason${recStatus.index}">Why this pick?</button>
                                <p class="rec-reason" id="recReason${recStatus.index}" hidden>${fn:escapeXml(rec.reason)}</p>
                            </c:if>

                            <button type="button" class="btn-gold rec-apply"
                                    data-rec-apply="${fn:escapeXml(rec.playerId)}"
                                    data-rec-replaces="${fn:escapeXml(rec.replacesPlayerId)}">Apply to transfer</button>
                        </article>
                    </c:forEach>
                </div>
            </section>
        </c:if>

        <a class="tf-history-link" href="${pageContext.request.contextPath}/transfers/history?teamId=${fn:escapeXml(teamId)}">View transfer history &rarr;</a>

    </div>
</main>

<script src="${pageContext.request.contextPath}/assets/js/transfers.js"></script>
</body>
</html>
