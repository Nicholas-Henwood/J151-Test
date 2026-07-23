<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>

<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Change Password - Fantasy TryTons</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/theme.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/sidebar.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/catalog.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/change-password.css">
</head>
<body class="catalog-page cp-page">

<c:set var="activeNav" value="profile" scope="request" />
<%@ include file="/WEB-INF/jspf/sidebar.jspf" %>

<main class="catalog-main">
    <div class="catalog-content">

        <header class="catalog-header">
            <div>
                <p class="catalog-eyebrow">Password &amp; security</p>
                <h1 class="brand-font">Change Password</h1>
            </div>
        </header>

        <c:if test="${not empty error}">
            <p class="cp-alert" role="alert"><c:out value="${error}" /></p>
        </c:if>
        <c:if test="${not empty success}">
            <p class="cp-alert cp-alert-success" role="status"><c:out value="${success}" /></p>
        </c:if>

        <section class="panel">
            <p class="cp-helper">Enter your current password, then choose a new one.</p>

            <form class="cp-form" method="post" action="${pageContext.request.contextPath}/profile/change-password" id="changePasswordForm">
                <label class="field-label" for="currentPassword">Current password</label>
                <div class="field-row">
                    <span class="field-icon" aria-hidden="true">
                        <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><rect x="5" y="11" width="14" height="10" rx="2"/><path d="M8 11V7a4 4 0 0 1 8 0v4"/></svg>
                    </span>
                    <input class="field-input" type="password" id="currentPassword" name="currentPassword"
                           required autocomplete="current-password" placeholder="&bull;&bull;&bull;&bull;&bull;&bull;&bull;&bull;">
                    <button type="button" class="password-toggle" data-toggle="currentPassword" aria-label="Show password">
                        <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M1 12s4-7 11-7 11 7 11 7-4 7-11 7-11-7-11-7z"/><circle cx="12" cy="12" r="3"/></svg>
                    </button>
                </div>

                <label class="field-label" for="newPassword">New password</label>
                <div class="field-row">
                    <span class="field-icon" aria-hidden="true">
                        <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><rect x="5" y="11" width="14" height="10" rx="2"/><path d="M8 11V7a4 4 0 0 1 8 0v4"/></svg>
                    </span>
                    <input class="field-input" type="password" id="newPassword" name="newPassword"
                           required autocomplete="new-password" placeholder="&bull;&bull;&bull;&bull;&bull;&bull;&bull;&bull;">
                    <button type="button" class="password-toggle" data-toggle="newPassword" aria-label="Show password">
                        <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M1 12s4-7 11-7 11 7 11 7-4 7-11 7-11-7-11-7z"/><circle cx="12" cy="12" r="3"/></svg>
                    </button>
                </div>

                <label class="field-label" for="confirmPassword">Confirm new password</label>
                <div class="field-row">
                    <span class="field-icon" aria-hidden="true">
                        <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><rect x="5" y="11" width="14" height="10" rx="2"/><path d="M8 11V7a4 4 0 0 1 8 0v4"/></svg>
                    </span>
                    <%-- Confirmation is client-side only; only currentPassword/newPassword are sent. --%>
                    <input class="field-input" type="password" id="confirmPassword" name="confirmPassword"
                           required autocomplete="new-password" placeholder="&bull;&bull;&bull;&bull;&bull;&bull;&bull;&bull;">
                    <button type="button" class="password-toggle" data-toggle="confirmPassword" aria-label="Show password">
                        <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M1 12s4-7 11-7 11 7 11 7-4 7-11 7-11-7-11-7z"/><circle cx="12" cy="12" r="3"/></svg>
                    </button>
                </div>

                <p class="cp-mismatch" role="alert" id="passwordMismatchWarning" hidden>New password and confirmation do not match.</p>

                <button type="submit" class="btn-gold cp-save">Change password</button>
            </form>

            <a class="cp-back" href="${pageContext.request.contextPath}/profile">&larr; Back to profile</a>
        </section>

    </div>
</main>

<script src="${pageContext.request.contextPath}/assets/js/change-password.js"></script>
</body>
</html>
