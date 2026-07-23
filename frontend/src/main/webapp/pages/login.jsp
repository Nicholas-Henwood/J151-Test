<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>

<!DOCTYPE html>
<html lang="en">
<head>
<meta charset="UTF-8">
<meta name="viewport" content="width=device-width, initial-scale=1.0">
<title>TryTons - Login</title>
<link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css">
<link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/theme.css">
<link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/login.css">
</head>

<body class="login-page">
<div class="container-fluid">
    <div class="row min-vh-100">

        <div class="col-lg-6 d-flex align-items-center justify-content-center py-5 px-4">
            <main class="w-100" style="max-width:360px;">
                <div class="auth-rule"></div>
                <p class="auth-eyebrow">Fantasy TryTons League</p>
                <h1 class="brand-font auth-heading mb-2">Welcome back</h1>
                <p class="auth-subtext mb-4">Sign in to manage your squad and chase the table.</p>

                <c:if test="${param.registered == '1'}">
                    <div class="auth-alert auth-alert-success mb-3" role="status">
                        Registration successful. Please log in with your new account.
                    </div>
                </c:if>
                <c:if test="${param.expired == '1'}">
                    <div class="auth-alert auth-alert-notice mb-3" role="alert">
                        Your session has expired. Please log in again.
                    </div>
                </c:if>
                <c:if test="${not empty error}">
                    <div class="auth-alert auth-alert-danger mb-3" role="alert">
                        <c:out value="${error}" />
                    </div>
                </c:if>

                <form method="post" action="${pageContext.request.contextPath}/login" id="loginForm">
                    <div class="mb-4">
                        <label for="identifier" class="field-label">Username or email</label>
                        <div class="field-row">
                            <span class="field-icon" aria-hidden="true">
                                <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M22 6l-10 7L2 6"/><path d="M2 6h20v12H2z"/></svg>
                            </span>
                            <input
                                type="text"
                                id="identifier"
                                name="identifier"
                                class="field-input"
                                value="${fn:escapeXml(identifier)}"
                                required
                                autocomplete="username"
                                placeholder="you@example.com">
                        </div>
                    </div>

                    <div class="mb-2">
                        <div class="d-flex justify-content-between align-items-baseline mb-2">
                            <label for="password" class="field-label mb-0">Password</label>
                            <a href="#" class="forgot-link">Forgot password?</a>
                        </div>
                        <div class="field-row">
                            <span class="field-icon" aria-hidden="true">
                                <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><rect x="5" y="11" width="14" height="10" rx="2"/><path d="M8 11V7a4 4 0 0 1 8 0v4"/></svg>
                            </span>
                            <input
                                type="password"
                                id="password"
                                name="password"
                                class="field-input"
                                required
                                autocomplete="current-password"
                                placeholder="&bull;&bull;&bull;&bull;&bull;&bull;&bull;&bull;">
                            <button type="button" class="password-toggle" id="passwordToggle" aria-label="Show password">
                                <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M1 12s4-7 11-7 11 7 11 7-4 7-11 7-11-7-11-7z"/><circle cx="12" cy="12" r="3"/></svg>
                            </button>
                        </div>
                    </div>

                    <div class="d-flex align-items-center mb-4 mt-4" style="gap:8px;">
                        <input class="login-check" type="checkbox" id="remember" name="rememberMe">
                        <label for="remember" class="login-check-label mb-0">Remember me on this device</label>
                    </div>

                    <button type="submit" name="submit" value="login" class="btn-gold w-100" id="loginSubmit">
                        <span class="btn-spinner" aria-hidden="true"></span><span id="loginSubmitLabel">Enter the Stadium &rarr;</span>
                    </button>
                </form>

                <p class="auth-footer-text mt-4 mb-0">
                    New to the league? <a href="${pageContext.request.contextPath}/register">Create an account</a>
                </p>
            </main>
        </div>

        <div class="col-lg-6 d-none d-lg-flex align-items-center justify-content-center auth-visual login-visual-bg" aria-hidden="true">
            <div class="auth-visual-content p-5">
                <img src="${pageContext.request.contextPath}/assets/images/Trytons-Logo.png" alt="Fantasy TryTons League" class="auth-visual-logo">
                <div class="auth-visual-rule"></div>
                <p class="brand-font auth-visual-tagline mb-3">Build your squad. Back your form. Climb the table.</p>
                <p class="auth-visual-support">Every round is a new chance to prove your XI.</p>
            </div>
        </div>

    </div>
</div>
<script src="${pageContext.request.contextPath}/assets/js/login.js"></script>
<script src="${pageContext.request.contextPath}/assets/js/page-transitions.js"></script>
</body>
</html>
