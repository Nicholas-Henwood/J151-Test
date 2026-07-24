<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>

<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>User Management - Fantasy TryTons</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/theme.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/sidebar.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/catalog.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/admin-users.css">
</head>
<body class="catalog-page ausr-page">

<c:set var="activeNav" value="admin-users" scope="request" />
<%@ include file="/WEB-INF/jspf/sidebar.jspf" %>

<main class="catalog-main" id="adminUsers">
    <div class="catalog-content">

        <header class="catalog-header">
            <div>
                <p class="catalog-eyebrow">Administration</p>
                <h1 class="brand-font">User Management</h1>
            </div>
        </header>

        <c:if test="${not empty success}">
            <p class="ausr-alert ausr-alert-success" role="status"><c:out value="${success}" /></p>
        </c:if>
        <c:if test="${not empty error}">
            <p class="ausr-alert ausr-alert-error" role="alert"><c:out value="${error}" /></p>
        </c:if>

        <%-- ---------- Search ---------- --%>
        <section id="userSearch">
            <form action="${pageContext.request.contextPath}/admin/users" method="get" id="userSearchForm" class="ausr-search-row">
                <label class="ausr-search" for="searchTerm">
                    <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true"><circle cx="11" cy="11" r="7"/><path d="M21 21l-4.3-4.3"/></svg>
                    <input type="text" id="searchTerm" name="searchTerm" value="${fn:escapeXml(searchTerm)}"
                           placeholder="Search email, username or role" aria-label="Search email, username or role">
                </label>
                <button type="submit" class="btn-gold ausr-search-btn">Search</button>
            </form>
        </section>

        <%-- ---------- Users ---------- --%>
        <section id="userList">
            <div class="ausr-section-head">
                <h2 class="ausr-section-title">Users</h2>
                <span class="ausr-rule-line"></span>
                <span class="ausr-count">${fn:length(users)} user${fn:length(users) == 1 ? '' : 's'}</span>
            </div>

            <c:choose>
                <c:when test="${empty users}">
                    <p class="ausr-empty" id="usersEmptyState">No users found.</p>
                </c:when>
                <c:otherwise>
                    <div class="utbl" id="usersTable">
                        <div class="utbl-scroll">
                            <div class="utbl-head">
                                <span>Email</span>
                                <span>Username</span>
                                <span>Role</span>
                                <span>Status</span>
                                <span aria-hidden="true"></span>
                            </div>

                            <c:forEach var="user" items="${users}">
                                <%-- Avatar initials come from the email's local part. --%>
                                <c:set var="local" value="${fn:substringBefore(user.email, '@')}" />
                                <c:set var="localParts" value="${fn:split(local, '._-')}" />
                                <div class="utbl-row">
                                    <span class="utbl-c-email">
                                        <span class="utbl-avatar" aria-hidden="true"><c:if test="${fn:length(localParts) > 0}">${fn:toUpperCase(fn:substring(localParts[0], 0, 1))}<c:if test="${fn:length(localParts) > 1}">${fn:toUpperCase(fn:substring(localParts[1], 0, 1))}</c:if></c:if></span>
                                        <span class="utbl-email" title="${fn:escapeXml(user.email)}"><c:out value="${user.email}" /></span>
                                    </span>
                                    <span class="utbl-username" title="${fn:escapeXml(user.username)}"><c:out value="${user.username}" /></span>
                                    <span>
                                        <span class="utbl-role ${user.role == 'ADMINISTRATOR' ? 'is-admin' : 'is-user'}"><c:out value="${user.role}" /></span>
                                    </span>
                                    <span>
                                        <span class="utbl-status ${user.active ? 'is-active' : 'is-inactive'}">${user.active ? 'Active' : 'Inactive'}</span>
                                    </span>
                                    <span class="utbl-action">
                                        <form action="${pageContext.request.contextPath}/admin/users" method="post">
                                            <input type="hidden" name="searchTerm" value="${fn:escapeXml(searchTerm)}">
                                            <input type="hidden" name="userId" value="${user.userId}">
                                            <c:choose>
                                                <c:when test="${user.active}">
                                                    <input type="hidden" name="isActive" value="false">
                                                    <button type="submit" class="utbl-toggle is-deactivate">Deactivate</button>
                                                </c:when>
                                                <c:otherwise>
                                                    <input type="hidden" name="isActive" value="true">
                                                    <button type="submit" class="utbl-toggle is-activate">Activate</button>
                                                </c:otherwise>
                                            </c:choose>
                                        </form>
                                    </span>
                                </div>
                            </c:forEach>
                        </div>
                    </div>
                </c:otherwise>
            </c:choose>
        </section>

    </div>
</main>

</body>
</html>
