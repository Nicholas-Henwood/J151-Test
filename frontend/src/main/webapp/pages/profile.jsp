<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>

<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>My Profile - Fantasy TryTons</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/theme.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/sidebar.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/catalog.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/profile.css">
</head>
<body class="catalog-page pf-page">

<c:set var="activeNav" value="profile" scope="request" />
<%@ include file="/WEB-INF/jspf/sidebar.jspf" %>

<main class="catalog-main">
    <div class="catalog-content">

        <header class="catalog-header">
            <div>
                <p class="catalog-eyebrow">Your account</p>
                <h1 class="brand-font">My Profile</h1>
            </div>
        </header>

        <c:if test="${not empty error}">
            <p class="pf-alert" role="alert"><c:out value="${error}" /></p>
        </c:if>
        <c:if test="${not empty success}">
            <p class="pf-alert pf-alert-success" role="status"><c:out value="${success}" /></p>
        </c:if>

        <c:choose>
            <c:when test="${empty profile}">
                <p class="pf-fallback">Your profile could not be loaded right now. Please try again later.</p>
            </c:when>

            <c:otherwise>
                <c:set var="uname" value="${empty profile.username ? '' : profile.username}" />
                <c:set var="nameParts" value="${fn:split(uname, ' ')}" />
                <c:set var="initials"><c:if test="${fn:length(nameParts) > 0}">${fn:toUpperCase(fn:substring(nameParts[0], 0, 1))}<c:if test="${fn:length(nameParts) > 1}">${fn:toUpperCase(fn:substring(nameParts[fn:length(nameParts) - 1], 0, 1))}</c:if></c:if></c:set>
                <c:set var="isAdmin" value="${profile.role == 'ADMINISTRATOR'}" />

                <%-- ---------- Profile hero ---------- --%>
                <section class="pf-hero">
                    <div class="pf-banner"></div>
                    <div class="pf-identity">
                        <span class="pf-avatar">
                            <span class="pf-avatar-initials" id="heroAvatarInitials">${initials}</span>
                            <%-- Initials sit underneath; the image covers them only when there
                                 is a picture to show. The src is emitted only when non-empty —
                                 an empty src makes the browser load the page as an image and
                                 draw a broken-image icon over the initials. onerror hides it if
                                 the URL is set but unreachable; profile.js manages it live. --%>
                            <img id="heroAvatarImage" alt="" onerror="this.hidden=true"
                                 <c:choose>
                                     <c:when test="${not empty profile.profilePic}">src="${fn:escapeXml(profile.profilePic)}"</c:when>
                                     <c:otherwise>hidden</c:otherwise>
                                 </c:choose>>
                        </span>

                        <div class="pf-identity-text">
                            <p class="pf-username" id="heroUsername">${fn:escapeXml(uname)}</p>
                            <p class="pf-email">${fn:escapeXml(profile.email)}</p>
                        </div>

                        <div class="pf-pills">
                            <span class="pf-pill ${isAdmin ? 'pf-pill-gold' : 'pf-pill-silver'}">${fn:escapeXml(profile.role)}</span>
                            <c:choose>
                                <c:when test="${profile.isActive}">
                                    <span class="pf-pill pf-pill-active">Active</span>
                                </c:when>
                                <c:otherwise>
                                    <span class="pf-pill pf-pill-inactive">Inactive</span>
                                </c:otherwise>
                            </c:choose>
                        </div>
                    </div>

                    <div class="pf-facts">
                        <div class="pf-fact">
                            <span class="pf-fact-label">Registration</span>
                            <span class="pf-fact-value">${empty profile.registrationStatus ? '—' : fn:escapeXml(profile.registrationStatus)}</span>
                        </div>
                        <div class="pf-fact">
                            <span class="pf-fact-label">Member since</span>
                            <span class="pf-fact-value">${empty registrationDateLabel ? '—' : registrationDateLabel}</span>
                        </div>
                        <div class="pf-fact">
                            <span class="pf-fact-label">Last login</span>
                            <span class="pf-fact-value">${empty lastLoginLabel ? 'Never' : lastLoginLabel}</span>
                        </div>
                    </div>
                </section>

                <%-- ---------- Two-column body ---------- --%>
                <div class="pf-columns">

                    <%-- Account details --%>
                    <section class="panel">
                        <h2 class="pf-panel-heading">Account details</h2>
                        <dl class="pf-dl">
                            <dt>Email</dt>
                            <dd>${fn:escapeXml(profile.email)}</dd>

                            <dt>Username</dt>
                            <dd>${fn:escapeXml(uname)}</dd>

                            <dt>Role</dt>
                            <dd>${fn:escapeXml(profile.role)}</dd>

                            <dt>Account status</dt>
                            <dd>
                                <c:choose>
                                    <c:when test="${profile.isActive}"><span class="pf-pill pf-pill-active">Active</span></c:when>
                                    <c:otherwise><span class="pf-pill pf-pill-inactive">Inactive</span></c:otherwise>
                                </c:choose>
                            </dd>

                            <dt>Registration status</dt>
                            <dd>${empty profile.registrationStatus ? '—' : fn:escapeXml(profile.registrationStatus)}</dd>

                            <dt>Registered on</dt>
                            <dd>${empty registrationDateLabel ? '—' : registrationDateLabel}</dd>

                            <dt>Last login</dt>
                            <dd>${empty lastLoginLabel ? 'Never' : lastLoginLabel}</dd>
                        </dl>
                    </section>

                    <%-- Update profile + security --%>
                    <div>
                        <section class="panel">
                            <h2 class="pf-panel-heading">Update profile</h2>
                            <p class="pf-panel-helper">Change a field and save &mdash; anything you leave as-is stays unchanged.</p>

                            <%-- Every input is pre-filled: the backend treats a null field as
                                 "leave unchanged", so a no-op save must resend current values. --%>
                            <form class="pf-form" action="${pageContext.request.contextPath}/profile" method="post" id="profileForm">
                                <label class="field-label" for="username">Username</label>
                                <div class="field-row">
                                    <span class="field-icon">
                                        <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true"><path d="M19 21v-2a4 4 0 0 0-4-4H9a4 4 0 0 0-4 4v2"/><circle cx="12" cy="7" r="4"/></svg>
                                    </span>
                                    <input class="field-input" type="text" id="username" name="username"
                                           value="${fn:escapeXml(uname)}" autocomplete="username">
                                </div>

                                <label class="field-label" for="email">Email</label>
                                <div class="field-row">
                                    <span class="field-icon">
                                        <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true"><rect x="3" y="5" width="18" height="14" rx="2"/><path d="M3 7l9 6 9-6"/></svg>
                                    </span>
                                    <input class="field-input" type="email" id="email" name="email"
                                           value="${fn:escapeXml(profile.email)}" autocomplete="email">
                                </div>

                                <label class="field-label" for="profilePic">Profile picture URL</label>
                                <div class="field-row">
                                    <span class="field-icon">
                                        <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true"><rect x="3" y="3" width="18" height="18" rx="2"/><circle cx="8.5" cy="8.5" r="1.5"/><path d="M21 15l-5-5L5 21"/></svg>
                                    </span>
                                    <input class="field-input" type="url" id="profilePic" name="profilePic"
                                           value="${fn:escapeXml(profile.profilePic)}" placeholder="https://…">
                                </div>

                                <button type="submit" class="btn-gold pf-save">Save changes</button>
                            </form>
                        </section>

                        <section class="panel pf-security">
                            <span class="pf-security-icon" aria-hidden="true">
                                <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><rect x="4" y="10" width="16" height="10" rx="2"/><path d="M8 10V7a4 4 0 0 1 8 0v3"/></svg>
                            </span>
                            <div class="pf-security-text">
                                <p class="pf-security-title">Password &amp; security</p>
                                <p class="pf-security-sub">Keep your account safe with a strong password.</p>
                            </div>
                            <a class="pf-security-link" href="${pageContext.request.contextPath}/profile/change-password">Change password &rarr;</a>
                        </section>
                    </div>

                </div>
            </c:otherwise>
        </c:choose>

    </div>
</main>

<script src="${pageContext.request.contextPath}/assets/js/profile.js"></script>
</body>
</html>
