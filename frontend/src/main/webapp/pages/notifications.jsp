<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>

<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Notifications - Fantasy TryTons</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/theme.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/sidebar.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/catalog.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/notifications.css">
</head>
<body class="catalog-page nt-page">

<c:set var="activeNav" value="notifications" scope="request" />
<%@ include file="/WEB-INF/jspf/sidebar.jspf" %>

<%-- Type families drive the badge tint and label colour. No enum value is a
     substring of another, so a contains test is unambiguous here. --%>
<c:set var="goldTypes" value="POINTS_UPDATE,LEADERBOARD_CHANGE,SIMULATED_RESULT,LEAGUE_INVITATION" />
<c:set var="greenTypes" value="MATCHUP_RESULT" />
<c:set var="redTypes" value="TRANSFER_DEADLINE,ROUND_LOCK,PLAYER_AVAILABILITY" />

<main class="catalog-main">
    <div class="catalog-content">

        <header class="catalog-header">
            <div>
                <p class="catalog-eyebrow">Stay in the loop</p>
                <div class="nt-title-row">
                    <h1 class="brand-font">Notifications</h1>
                    <c:if test="${unreadCount > 0}">
                        <span class="nt-unread-pill">${unreadCount} unread</span>
                    </c:if>
                </div>
            </div>
        </header>

        <c:if test="${not empty error}">
            <p class="catalog-error" role="alert"><c:out value="${error}" /></p>
        </c:if>
        <c:if test="${not empty success}">
            <p class="catalog-error" role="status" style="border-left-color:#5fae7a"><c:out value="${success}" /></p>
        </c:if>

        <%-- ---------- Action bar ---------- --%>
        <div class="nt-actions">
            <form action="${pageContext.request.contextPath}/notifications" method="get">
                <label class="nt-toggle" for="unreadOnlyToggle">
                    <input type="checkbox" id="unreadOnlyToggle" name="unreadOnly" value="true"
                           ${unreadOnly ? 'checked' : ''}>
                    <span class="nt-track" aria-hidden="true"></span>
                    <span>Show unread only</span>
                </label>
            </form>

            <form action="${pageContext.request.contextPath}/notifications" method="post">
                <input type="hidden" name="action" value="markAllAsRead">
                <button type="submit" class="nt-markall" ${unreadCount > 0 ? '' : 'disabled'}>Mark all as read</button>
            </form>
        </div>

        <c:choose>
            <c:when test="${empty notifications}">
                <%-- ---------- Empty state ---------- --%>
                <div class="nt-empty" id="notificationsEmptyState">
                    <span class="nt-empty-icon">
                        <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true"><path d="M18 8a6 6 0 0 0-12 0c0 7-3 9-3 9h18s-3-2-3-9"/><path d="M13.7 21a2 2 0 0 1-3.4 0"/></svg>
                    </span>
                    <c:choose>
                        <c:when test="${unreadOnly}">
                            <h2>You're all caught up</h2>
                            <p>No unread notifications. Turn off the filter to see everything.</p>
                        </c:when>
                        <c:otherwise>
                            <h2>You have no notifications yet</h2>
                            <p>New alerts about points, matchups and transfers will show here.</p>
                        </c:otherwise>
                    </c:choose>
                </div>
            </c:when>

            <c:otherwise>
                <%-- ---------- Grouped feed ---------- --%>
                <c:forEach var="group" items="${notificationGroups}">
                    <section class="nt-group">
                        <p class="nt-group-label">${fn:escapeXml(group.key)}</p>
                        <div class="nt-list">
                            <c:forEach var="note" items="${group.value}">
                                <c:set var="type" value="${empty note.type ? 'SYSTEM' : note.type}" />
                                <c:set var="family" value="${fn:contains(goldTypes, type) ? 'gold'
                                                            : (fn:contains(greenTypes, type) ? 'green'
                                                            : (fn:contains(redTypes, type) ? 'red' : 'silver'))}" />

                                <article class="n-card ${note.read ? 'is-read' : 'is-unread'}">
                                    <span class="n-icon n-icon-${family}" aria-hidden="true">
                                        <c:choose>
                                            <c:when test="${type == 'POINTS_UPDATE'}"><svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round"><path d="M12 3l2.6 5.6 6 .8-4.4 4.2 1.1 6-5.3-2.9-5.3 2.9 1.1-6L3.4 9.4l6-.8z"/></svg></c:when>
                                            <c:when test="${type == 'LEADERBOARD_CHANGE'}"><svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round"><path d="M3 17l6-6 4 4 7-7"/><path d="M14 8h6v6"/></svg></c:when>
                                            <c:when test="${type == 'SIMULATED_RESULT'}"><svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round"><path d="M13 2L4 14h7l-1 8 9-12h-7z"/></svg></c:when>
                                            <c:when test="${type == 'LEAGUE_INVITATION'}"><svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round"><rect x="3" y="5" width="18" height="14" rx="2"/><path d="M3 7l9 6 9-6"/></svg></c:when>
                                            <c:when test="${type == 'MATCHUP_RESULT'}"><svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round"><path d="M8 21h8"/><path d="M12 17v4"/><path d="M7 4h10v5a5 5 0 0 1-10 0z"/><path d="M17 5h3v2a3 3 0 0 1-3 3"/><path d="M7 5H4v2a3 3 0 0 0 3 3"/></svg></c:when>
                                            <c:when test="${type == 'TRANSFER_DEADLINE'}"><svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round"><circle cx="12" cy="12" r="9"/><path d="M12 7v5l3 2"/></svg></c:when>
                                            <c:when test="${type == 'ROUND_LOCK'}"><svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round"><rect x="4" y="10" width="16" height="10" rx="2"/><path d="M8 10V7a4 4 0 0 1 8 0v3"/></svg></c:when>
                                            <c:when test="${type == 'PLAYER_AVAILABILITY'}"><svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round"><path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"/><circle cx="12" cy="7" r="4"/></svg></c:when>
                                            <c:when test="${type == 'CHAT_MESSAGE'}"><svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round"><path d="M21 12a8 8 0 0 1-8 8H7l-4 3V12a8 8 0 0 1 8-8h2a8 8 0 0 1 8 8z"/></svg></c:when>
                                            <c:when test="${type == 'REPORT_UPDATE'}"><svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round"><path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/><path d="M14 2v6h6"/><path d="M8 13h8"/><path d="M8 17h5"/></svg></c:when>
                                            <c:otherwise><svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round"><circle cx="12" cy="12" r="9"/><path d="M12 11v5"/><path d="M12 8h.01"/></svg></c:otherwise>
                                        </c:choose>
                                    </span>

                                    <div class="n-body">
                                        <div class="n-type-row">
                                            <span class="n-type n-type-${family}">${fn:escapeXml(fn:replace(type, '_', ' '))}</span>
                                            <c:if test="${not note.read}"><span class="n-dot" aria-label="Unread"></span></c:if>
                                        </div>
                                        <p class="n-message">${fn:escapeXml(note.body)}</p>

                                        <%-- Deep link, when the notification points at something.
                                             FIXTURE, LEAGUE and PLAYER are the types the backend sets. --%>
                                        <c:if test="${not empty note.relatedEntityId}">
                                            <c:choose>
                                                <c:when test="${note.relatedEntityType == 'FIXTURE'}">
                                                    <a class="n-link" href="${pageContext.request.contextPath}/fixture?submit=fixture&amp;fixtureId=${note.relatedEntityId}">View fixture &rarr;</a>
                                                </c:when>
                                                <c:when test="${note.relatedEntityType == 'LEAGUE'}">
                                                    <a class="n-link" href="${pageContext.request.contextPath}/league?leagueId=${note.relatedEntityId}">View league &rarr;</a>
                                                </c:when>
                                                <c:when test="${note.relatedEntityType == 'PLAYER'}">
                                                    <a class="n-link" href="${pageContext.request.contextPath}/player?submit=player&amp;playerId=${note.relatedEntityId}">View player &rarr;</a>
                                                </c:when>
                                            </c:choose>
                                        </c:if>
                                    </div>

                                    <div class="n-side">
                                        <span class="n-time">${fn:escapeXml(relativeTimes[note.notificationId])}</span>
                                        <c:if test="${not note.read}">
                                            <form class="n-mark-form" action="${pageContext.request.contextPath}/notifications" method="post">
                                                <input type="hidden" name="action" value="markAsRead">
                                                <input type="hidden" name="notificationId" value="${note.notificationId}">
                                                <button type="submit" class="n-mark">Mark as read</button>
                                            </form>
                                        </c:if>
                                    </div>
                                </article>
                            </c:forEach>
                        </div>
                    </section>
                </c:forEach>
            </c:otherwise>
        </c:choose>

    </div>
</main>

<script src="${pageContext.request.contextPath}/assets/js/notifications.js"></script>
</body>
</html>
