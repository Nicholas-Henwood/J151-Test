<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>

<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Dashboard - Fantasy TryTons</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/theme.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/sidebar.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/dashboard.css">
</head>
<body class="dashboard">

<c:set var="activeNav" value="home" scope="request" />
<%@ include file="/WEB-INF/jspf/sidebar.jspf" %>

<main class="dashboard-main">

    <section class="dashboard-hero">
        <div class="dashboard-hero-content">
            <p class="dashboard-hero-eyebrow">Fantasy TryTons League</p>
            <c:choose>
                <c:when test="${not empty sessionScope.username}">
                    <h1 class="brand-font">Welcome back, ${fn:escapeXml(sessionScope.username)}</h1>
                </c:when>
                <c:otherwise>
                    <h1 class="brand-font">Welcome to Fantasy TryTons</h1>
                </c:otherwise>
            </c:choose>
            <p class="dashboard-hero-sub">Jump back into your league from here.</p>
            <a class="btn-gold" href="${pageContext.request.contextPath}/fixtures?submit=fixtures">View Fixtures &rarr;</a>
        </div>
    </section>

    <section class="dashboard-grid">

        <a class="dashboard-card card-create-team" href="${pageContext.request.contextPath}/create-team">
            <span class="dashboard-card-icon">
                <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M16 21v-2a4 4 0 0 0-4-4H6a4 4 0 0 0-4 4v2"/><circle cx="9" cy="7" r="4"/><path d="M19 8v6"/><path d="M22 11h-6"/></svg>
            </span>
            <span class="dashboard-card-title brand-font">Create Team <span class="dashboard-card-arrow">&rarr;</span></span>
            <p class="dashboard-card-desc">Build your squad and get ready to compete.</p>
        </a>

        <a class="dashboard-card card-leagues" href="${pageContext.request.contextPath}/leagues">
            <span class="dashboard-card-icon">
                <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M8 21h8"/><path d="M12 17v4"/><path d="M7 4h10v5a5 5 0 0 1-10 0z"/><path d="M17 5h3v2a3 3 0 0 1-3 3"/><path d="M7 5H4v2a3 3 0 0 0 3 3"/></svg>
            </span>
            <span class="dashboard-card-title brand-font">Leagues <span class="dashboard-card-arrow">&rarr;</span></span>
            <p class="dashboard-card-desc">Browse, create or join a fantasy league.</p>
        </a>

        <a class="dashboard-card card-fixtures" href="${pageContext.request.contextPath}/fixtures?submit=fixtures">
            <span class="dashboard-card-icon">
                <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><rect x="3" y="4" width="18" height="18" rx="2"/><path d="M16 2v4"/><path d="M8 2v4"/><path d="M3 10h18"/></svg>
            </span>
            <span class="dashboard-card-title brand-font">Fixtures <span class="dashboard-card-arrow">&rarr;</span></span>
            <p class="dashboard-card-desc">See upcoming and completed fixtures.</p>
        </a>

        <a class="dashboard-card card-transfers" href="${pageContext.request.contextPath}/transfers">
            <span class="dashboard-card-icon">
                <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M17 3l4 4-4 4"/><path d="M21 7H8"/><path d="M7 21l-4-4 4-4"/><path d="M3 17h13"/></svg>
            </span>
            <span class="dashboard-card-title brand-font">Transfers <span class="dashboard-card-arrow">&rarr;</span></span>
            <p class="dashboard-card-desc">Manage your squad's transfers.</p>
        </a>

        <a class="dashboard-card card-notifications" href="${pageContext.request.contextPath}/notifications">
            <span class="dashboard-card-icon">
                <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M18 8a6 6 0 0 0-12 0c0 7-3 9-3 9h18s-3-2-3-9"/><path d="M13.7 21a2 2 0 0 1-3.4 0"/></svg>
            </span>
            <span class="dashboard-card-title brand-font">Notifications <span class="dashboard-card-arrow">&rarr;</span></span>
            <p class="dashboard-card-desc">Check your latest notifications.</p>
        </a>

        <a class="dashboard-card card-profile" href="${pageContext.request.contextPath}/profile">
            <span class="dashboard-card-icon">
                <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M19 21v-2a4 4 0 0 0-4-4H9a4 4 0 0 0-4 4v2"/><circle cx="12" cy="7" r="4"/></svg>
            </span>
            <span class="dashboard-card-title brand-font">Profile <span class="dashboard-card-arrow">&rarr;</span></span>
            <p class="dashboard-card-desc">View and update your account details.</p>
        </a>

    </section>

</main>
</body>
</html>
