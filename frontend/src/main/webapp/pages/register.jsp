<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>

<!DOCTYPE html>
<html lang="en">
<head>
<meta charset="UTF-8">
<meta name="viewport" content="width=device-width, initial-scale=1.0">
<title>TryTons - Register</title>
<link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css">
<link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/theme.css">
<link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/register.css">
</head>

<body class="register-page">
<div class="container-fluid">
    <div class="row min-vh-100">

        <div class="col-lg-6 d-flex align-items-center justify-content-center py-5 px-4 register-left-panel">
            <main class="w-100" style="max-width:360px;">
                <div class="auth-rule"></div>
                <p class="auth-eyebrow">Fantasy TryTons League</p>
                <h1 class="brand-font auth-heading mb-2">Create your account</h1>
                <p class="auth-subtext mb-4">Join the league and build your squad for the season ahead.</p>

                <c:if test="${not empty error}">
                    <div class="auth-alert auth-alert-danger mb-3" role="alert">
                        <c:out value="${error}" />
                    </div>
                </c:if>

                <form method="post" action="${pageContext.request.contextPath}/register" id="registerForm">
                    <div class="mb-4">
                        <label for="email" class="field-label">Email address</label>
                        <div class="field-row">
                            <span class="field-icon" aria-hidden="true">
                                <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M22 6l-10 7L2 6"/><path d="M2 6h20v12H2z"/></svg>
                            </span>
                            <input
                                type="email"
                                id="email"
                                name="email"
                                class="field-input"
                                value="${fn:escapeXml(email)}"
                                required
                                autocomplete="email"
                                placeholder="you@example.com">
                        </div>
                    </div>

                    <div class="mb-4">
                        <label for="username" class="field-label">Username</label>
                        <div class="field-row">
                            <span class="field-icon" aria-hidden="true">
                                <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"/><circle cx="12" cy="7" r="4"/></svg>
                            </span>
                            <input
                                type="text"
                                id="username"
                                name="username"
                                class="field-input"
                                value="${fn:escapeXml(username)}"
                                required
                                autocomplete="username"
                                placeholder="Choose a username">
                        </div>
                    </div>

                    <div class="mb-2">
                        <label for="rawPassword" class="field-label">Password</label>
                        <div class="field-row">
                            <span class="field-icon" aria-hidden="true">
                                <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><rect x="5" y="11" width="14" height="10" rx="2"/><path d="M8 11V7a4 4 0 0 1 8 0v4"/></svg>
                            </span>
                            <input
                                type="password"
                                id="rawPassword"
                                name="rawPassword"
                                class="field-input"
                                required
                                autocomplete="new-password"
                                placeholder="Create a password">
                            <button type="button" class="password-toggle" id="passwordToggle" aria-label="Show password">
                                <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M1 12s4-7 11-7 11 7 11 7-4 7-11 7-11-7-11-7z"/><circle cx="12" cy="12" r="3"/></svg>
                            </button>
                        </div>
                        <p class="field-help">Use at least 8 characters.</p>
                    </div>

                    <button type="submit" name="submit" value="register" class="btn-gold w-100 mt-3" id="registerSubmit">
                        <span class="btn-spinner" aria-hidden="true"></span><span id="registerSubmitLabel">Create account &rarr;</span>
                    </button>
                </form>

                <p class="auth-footer-text mt-4 mb-0">
                    Already have an account? <a href="${pageContext.request.contextPath}/login">Log in</a>
                </p>
            </main>
        </div>

        <div class="col-lg-6 d-none d-lg-flex align-items-center justify-content-center auth-visual register-visual-bg" aria-hidden="true">
            <div class="auth-visual-content p-5">
                <img src="${pageContext.request.contextPath}/assets/images/Trytons-Logo.png" alt="Fantasy TryTons League" class="auth-visual-logo">
                <div class="auth-visual-rule"></div>
                <p class="brand-font auth-visual-tagline mb-3">Pick your XV. Manage every round. Own the season.</p>
                <p class="auth-visual-support">Sign up in a minute and join the league.</p>
            </div>
        </div>

    </div>
</div>
<script src="${pageContext.request.contextPath}/assets/js/register.js"></script>
<script src="${pageContext.request.contextPath}/assets/js/page-transitions.js"></script>
</body>
</html>
