<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<%@ taglib prefix="t" tagdir="/WEB-INF/tags" %>

<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>${fn:escapeXml(player.playerName)} - Fantasy TryTons</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/theme.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/sidebar.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/catalog.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/player.css">
</head>
<body class="catalog-page pl-page">

<c:set var="activeNav" value="players" scope="request" />
<%@ include file="/WEB-INF/jspf/sidebar.jspf" %>

<main class="catalog-main">
    <div class="catalog-content">

        <a class="pl-back" href="${pageContext.request.contextPath}/players?submit=players">&larr; Back to players</a>

        <c:choose>
            <c:when test="${empty player}">
                <p class="pl-alert" role="alert">${empty error ? 'This player could not be loaded.' : fn:escapeXml(error)}</p>
            </c:when>

            <c:otherwise>
                <c:if test="${not empty error}">
                    <p class="pl-alert" role="alert"><c:out value="${error}" /></p>
                </c:if>
                <c:if test="${not empty availabilityMessage}">
                    <p class="pl-alert pl-success" role="status"><c:out value="${availabilityMessage}" /></p>
                </c:if>

                <c:set var="pname" value="${empty player.playerName ? '' : player.playerName}" />
                <c:set var="nameParts" value="${fn:split(pname, ' ')}" />
                <c:set var="clubName" value="${clubNamesById[player.clubId]}" />
                <c:set var="positionName" value="${positionNamesById[player.positionId]}" />
                <c:set var="isForward" value="${positionCategoriesById[player.positionId] == 'FORWARD'}" />

                <%-- Current availability status: the record's status when one has been set,
                     otherwise derived from isActive. --%>
                <c:set var="status" value="${empty availability ? (player.active ? 'ACTIVE' : 'UNAVAILABLE') : fn:toUpperCase(availability.status)}" />
                <c:set var="availClass" value="${status == 'ACTIVE' ? 'pl-avail-active'
                                              : (status == 'INJURED' ? 'pl-avail-injured'
                                              : (status == 'SUSPENDED' ? 'pl-avail-suspended' : 'pl-avail-unavailable'))}" />

                <%-- Ring geometry: r = (96 - strokeWidth 8) / 2 = 44; C = 2πr ≈ 276.46.
                     The gold arc is overall/100 of the circumference. --%>
                <c:set var="ringCirc" value="276.46" />
                <c:set var="ringFill" value="${(overallRating / 100.0) * ringCirc}" />

                <%-- ---------- Hero ---------- --%>
                <section class="pl-hero">
                    <div class="pl-hero-inner">
                        <span class="pl-portrait" aria-hidden="true"><c:if test="${fn:length(nameParts) > 0}">${fn:toUpperCase(fn:substring(nameParts[0], 0, 1))}<c:if test="${fn:length(nameParts) > 1}">${fn:toUpperCase(fn:substring(nameParts[fn:length(nameParts) - 1], 0, 1))}</c:if></c:if></span>

                        <div class="pl-identity">
                            <p class="pl-eyebrow">Player profile</p>
                            <h1 class="pl-name">${fn:escapeXml(pname)}</h1>
                            <div class="pl-tags">
                                <span class="pl-pos ${isForward ? 'pl-pos-fwd' : 'pl-pos-back'}">${fn:escapeXml(positionName)}</span>
                                <span class="pl-club">${fn:escapeXml(clubName)}</span>
                                <span class="pl-avail ${availClass}">${fn:escapeXml(fn:substring(status, 0, 1))}${fn:toLowerCase(fn:substring(status, 1, fn:length(status)))}</span>
                            </div>
                        </div>

                        <div class="pl-ring">
                            <svg viewBox="0 0 96 96" aria-hidden="true">
                                <defs>
                                    <linearGradient id="plRingGrad" x1="0" y1="0" x2="1" y2="1">
                                        <stop offset="0%" stop-color="#8a6a15"/>
                                        <stop offset="100%" stop-color="#e8c765"/>
                                    </linearGradient>
                                </defs>
                                <circle class="pl-ring-track" cx="48" cy="48" r="44"/>
                                <circle class="pl-ring-fill" cx="48" cy="48" r="44"
                                        style="stroke-dasharray: ${ringFill} ${ringCirc};"/>
                            </svg>
                            <div class="pl-ring-text">
                                <span class="pl-ring-value">${overallRating}</span>
                                <span class="pl-ring-label">Overall</span>
                            </div>
                        </div>
                    </div>
                </section>

                <%-- ---------- Key-stat cells ---------- --%>
                <section class="pl-stats">
                    <div class="pl-stat">
                        <span class="pl-stat-label">Value</span>
                        <span class="pl-stat-value is-gold"><t:money value="${player.value}" /></span>
                    </div>
                    <div class="pl-stat">
                        <span class="pl-stat-label">Current form</span>
                        <span class="pl-stat-value">${player.currentForm}</span>
                    </div>
                    <div class="pl-stat">
                        <span class="pl-stat-label">Fitness</span>
                        <span class="pl-stat-value">${player.fitness}</span>
                    </div>
                    <div class="pl-stat">
                        <span class="pl-stat-label">Consistency</span>
                        <span class="pl-stat-value">${player.consistency}</span>
                    </div>
                    <div class="pl-stat">
                        <span class="pl-stat-label">Status</span>
                        <span class="pl-stat-value">${player.active ? 'Active' : 'Inactive'}</span>
                    </div>
                </section>

                <%-- ---------- Two-column body ---------- --%>
                <div class="pl-body">

                    <%-- Ability ratings --%>
                    <section class="panel">
                        <h2 class="pl-panel-title">Ability ratings</h2>
                        <c:forEach var="ability" items="${[
                                {'name':'Attacking','v':player.attackingAbility},
                                {'name':'Defensive','v':player.defensiveAbility},
                                {'name':'Kicking','v':player.kickingAbility},
                                {'name':'Discipline','v':player.discipline},
                                {'name':'Consistency','v':player.consistency},
                                {'name':'Fitness','v':player.fitness}]}">
                            <div class="pl-rating">
                                <span class="pl-rating-name">${ability.name}</span>
                                <span class="pl-rating-track"><span class="pl-rating-fill" style="width:${ability.v}%"></span></span>
                                <span class="pl-rating-value">${ability.v}</span>
                            </div>
                        </c:forEach>
                    </section>

                    <div>
                        <%-- Availability panel --%>
                        <section class="panel">
                            <h2 class="pl-panel-title">Availability</h2>
                            <span class="pl-avail ${availClass}">${fn:escapeXml(fn:substring(status, 0, 1))}${fn:toLowerCase(fn:substring(status, 1, fn:length(status)))}</span>

                            <c:if test="${not empty availability}">
                                <p class="pl-avail-line">
                                    In effect from ${availability.effectiveDate}<c:if test="${not empty availability.endDate}"> to ${availability.endDate}</c:if>.
                                </p>
                                <c:if test="${not empty availability.notes}">
                                    <p class="pl-notes"><c:out value="${availability.notes}" /></p>
                                </c:if>
                            </c:if>
                        </section>

                        <%-- Admin set-availability form: shown only to administrators; the backend
                             enforces this too and rejects non-admin posts. --%>
                        <c:if test="${sessionScope.role == 'ADMINISTRATOR'}">
                            <section class="panel">
                                <h2 class="pl-panel-title">Set availability</h2>
                                <p class="pl-panel-helper">Admin only.</p>

                                <form method="post" action="${pageContext.request.contextPath}/player/availability">
                                    <input type="hidden" name="submit" value="player/availability">
                                    <input type="hidden" name="playerId" value="${fn:escapeXml(player.playerId)}">

                                    <div class="pl-field">
                                        <label class="pl-field-label" for="status">Status</label>
                                        <select class="pl-select" id="status" name="status">
                                            <option value="ACTIVE">Active</option>
                                            <option value="INJURED">Injured</option>
                                            <option value="SUSPENDED">Suspended</option>
                                            <option value="UNAVAILABLE">Unavailable</option>
                                        </select>
                                    </div>

                                    <div class="pl-field">
                                        <div class="pl-field-row">
                                            <div>
                                                <label class="pl-field-label" for="effectiveDate">Effective date</label>
                                                <input class="pl-input" type="date" id="effectiveDate" name="effectiveDate" required>
                                            </div>
                                            <div>
                                                <label class="pl-field-label" for="endDate">End date</label>
                                                <input class="pl-input" type="date" id="endDate" name="endDate">
                                            </div>
                                        </div>
                                    </div>

                                    <div class="pl-field">
                                        <label class="pl-field-label" for="notes">Notes</label>
                                        <textarea class="pl-textarea" id="notes" name="notes" placeholder="Optional context for this status change"></textarea>
                                    </div>

                                    <button type="submit" class="btn-gold pl-save">Save availability</button>
                                </form>
                            </section>
                        </c:if>
                    </div>

                </div>
            </c:otherwise>
        </c:choose>

    </div>
</main>
</body>
</html>
