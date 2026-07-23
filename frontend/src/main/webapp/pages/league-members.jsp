<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>

<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>League Members - Fantasy TryTons</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/theme.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/sidebar.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/catalog.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/league-members.css">
</head>
<body class="catalog-page lm-page">

<c:set var="activeNav" value="leagues" scope="request" />
<%@ include file="/WEB-INF/jspf/sidebar.jspf" %>

<main class="catalog-main">
    <div class="catalog-content">

        <c:choose>
            <c:when test="${empty leagueId}">
                <a class="lm-back" href="${pageContext.request.contextPath}/leagues">&larr; Back to leagues</a>
                <div class="lm-empty">
                    <h2>No league was selected</h2>
                    <p>Go back to your leagues and pick one to view its members.</p>
                </div>
            </c:when>

            <c:otherwise>
                <a class="lm-back" href="${pageContext.request.contextPath}/league?leagueId=${leagueId}">&larr; Back to league</a>

                <c:if test="${not empty error}">
                    <p class="lm-alert" role="alert"><c:out value="${error}" /></p>
                </c:if>
                <c:if test="${not empty success}">
                    <p class="lm-alert lm-success" role="status"><c:out value="${success}" /></p>
                </c:if>

                <c:set var="isPrivate" value="${not empty league and league.leagueType == 'PRIVATE'}" />
                <c:set var="cap" value="${empty league ? 0 : league.maxMembers}" />
                <c:set var="count" value="${empty memberCount ? 0 : memberCount}" />

                <%-- ---------- League header ---------- --%>
                <section class="panel">
                    <div class="lm-head">
                        <span class="lm-icon" aria-hidden="true">
                            <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M8 21h8"/><path d="M12 17v4"/><path d="M7 4h10v5a5 5 0 0 1-10 0z"/><path d="M17 5h3v2a3 3 0 0 1-3 3"/><path d="M7 5H4v2a3 3 0 0 0 3 3"/></svg>
                        </span>
                        <div class="lm-identity">
                            <p class="lm-eyebrow">League members</p>
                            <div class="lm-name-row">
                                <h1 class="lm-name">${empty league ? 'League' : fn:escapeXml(league.leagueName)}</h1>
                                <c:if test="${not empty league}">
                                    <span class="lm-type ${isPrivate ? 'lm-type-private' : 'lm-type-public'}">${isPrivate ? 'Private' : 'Public'}</span>
                                </c:if>
                            </div>
                            <c:if test="${not empty league.managerDisplayName}">
                                <p class="lm-managed">Managed by ${fn:escapeXml(league.managerDisplayName)}</p>
                            </c:if>
                        </div>

                        <%-- Invite code: private leagues only. --%>
                        <c:if test="${isPrivate and not empty league.leagueCode}">
                            <div class="lm-invite">
                                <span class="lm-invite-label">Invite code</span>
                                <span class="lm-code">
                                    <span class="lm-code-value">${fn:escapeXml(league.leagueCode)}</span>
                                    <button type="button" class="lm-copy" id="copyCode"
                                            data-code="${fn:escapeXml(league.leagueCode)}" aria-label="Copy invite code">
                                        <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><rect x="9" y="9" width="12" height="12" rx="2"/><path d="M5 15V5a2 2 0 0 1 2-2h10"/></svg>
                                    </button>
                                </span>
                            </div>
                        </c:if>
                    </div>

                    <c:if test="${not empty league}">
                        <div class="lm-stats">
                            <div class="lm-stat">
                                <span class="lm-stat-label">Members</span>
                                <span class="lm-stat-value is-gold">${count} / ${cap}</span>
                            </div>
                            <div class="lm-stat">
                                <span class="lm-stat-label">Spots left</span>
                                <span class="lm-stat-value">${cap - count < 0 ? 0 : cap - count}</span>
                            </div>
                            <div class="lm-stat">
                                <span class="lm-stat-label">Created</span>
                                <span class="lm-stat-value">${empty creationDateLabel ? '—' : creationDateLabel}</span>
                            </div>
                        </div>
                    </c:if>
                </section>

                <%-- ---------- Members list ---------- --%>
                <div class="lm-list-head">
                    <h2 class="lm-list-title">Members</h2>
                    <span class="lm-list-rule"></span>
                    <span class="lm-list-count">${fn:length(members)} ${fn:length(members) == 1 ? 'member' : 'members'}</span>
                </div>

                <c:choose>
                    <c:when test="${empty members}">
                        <div class="lm-empty">
                            <h2>No members yet</h2>
                            <p><c:choose><c:when test="${isPrivate}">Share the invite code above so managers can join.</c:when><c:otherwise>This league doesn't have any members yet.</c:otherwise></c:choose></p>
                        </div>
                    </c:when>

                    <c:otherwise>
                        <section class="panel lm-table">
                            <div class="lm-row lm-thead">
                                <span aria-hidden="true"></span>
                                <span>Manager</span>
                                <span>Team</span>
                                <span>Joined</span>
                                <span>Status</span>
                                <span aria-hidden="true"></span>
                            </div>
                            <div class="lm-body">
                                <c:forEach var="member" items="${members}">
                                    <c:set var="mname" value="${empty member.userDisplayName ? '' : member.userDisplayName}" />
                                    <c:set var="mparts" value="${fn:split(mname, ' ')}" />
                                    <c:set var="isManager" value="${not empty managerUserId and member.userId == managerUserId}" />

                                    <div class="lm-row">
                                        <span class="lm-avatar" aria-hidden="true"><c:if test="${fn:length(mparts) > 0}">${fn:toUpperCase(fn:substring(mparts[0], 0, 1))}<c:if test="${fn:length(mparts) > 1}">${fn:toUpperCase(fn:substring(mparts[fn:length(mparts) - 1], 0, 1))}</c:if></c:if></span>

                                        <span class="lm-manager">
                                            <span class="lm-manager-name" title="${fn:escapeXml(mname)}">${fn:escapeXml(mname)}</span>
                                            <c:if test="${isManager}"><span class="lm-badge">&#9733; Manager</span></c:if>
                                        </span>

                                        <span class="lm-team" title="${fn:escapeXml(member.teamDisplayName)}">${fn:escapeXml(member.teamDisplayName)}</span>

                                        <span class="lm-joined">${empty joinDateLabels[member.membershipId] ? '—' : joinDateLabels[member.membershipId]}</span>

                                        <span>
                                            <span class="lm-status ${member.active ? 'lm-status-active' : 'lm-status-left'}">${member.active ? 'Active' : 'Left'}</span>
                                        </span>

                                        <%-- Manager-only UI convenience: shown when the current user manages the
                                             league, the row is not the manager, and the member is active. The
                                             backend is the sole authority and rejects unauthorised removals. --%>
                                        <c:choose>
                                            <c:when test="${isLeagueManager and not isManager and member.active}">
                                                <form class="lm-remove-form" method="post" action="${pageContext.request.contextPath}/league/members">
                                                    <input type="hidden" name="submit" value="league/members/remove">
                                                    <input type="hidden" name="leagueId" value="${fn:escapeXml(leagueId)}">
                                                    <input type="hidden" name="membershipId" value="${fn:escapeXml(member.membershipId)}">
                                                    <button type="submit" class="lm-remove">Remove</button>
                                                </form>
                                            </c:when>
                                            <c:otherwise><span aria-hidden="true"></span></c:otherwise>
                                        </c:choose>
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

<script src="${pageContext.request.contextPath}/assets/js/league-members.js"></script>
</body>
</html>
