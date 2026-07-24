<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>

<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Fantasy TryTons League — Pick your XV. Own the season.</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/theme.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/landing.css">
</head>
<body class="landing-page">

<c:set var="ctx" value="${pageContext.request.contextPath}" />

<%-- ============================ Top nav ============================ --%>
<header class="ln-nav" id="ln-top">
    <div class="ln-container ln-nav-inner">
        <a class="ln-brand" href="${ctx}/landing">
            <img class="ln-brand-logo" src="${ctx}/assets/images/Trytons-Logo.png" alt="" aria-hidden="true">
            <span class="ln-brand-word brand-font">Fantasy TryTons</span>
        </a>
        <nav class="ln-nav-links" aria-label="Sections">
            <a href="#how-it-works">How it works</a>
            <a href="#public-leagues">Public leagues</a>
            <a href="#leaderboard">Leaderboard</a>
        </nav>
        <div class="ln-nav-cta">
            <a class="ln-login-link" href="${ctx}/login">Log in</a>
            <a class="ln-btn ln-btn-gold" href="${ctx}/register">Sign up</a>
        </div>
    </div>
</header>

<%-- ============================ Hero ============================ --%>
<section class="ln-hero">
    <div class="ln-hero-bg" style="background-image:url('${ctx}/assets/images/Stadium hero.jpg')" aria-hidden="true"></div>
    <div class="ln-hero-overlay" aria-hidden="true"></div>
    <div class="ln-container ln-hero-inner">
        <span class="ln-rule"></span>
        <p class="ln-eyebrow">Fantasy rugby, done properly</p>
        <h1 class="ln-hero-title brand-font">
            Pick your XV.<br>
            Manage every round.<br>
            <span class="is-gold">Own the season.</span>
        </h1>
        <p class="ln-hero-sub">
            Draft a squad of real players inside a fixed budget, make weekly transfers, join
            leagues with your mates, and climb the table as every round is scored.
        </p>
        <div class="ln-hero-actions">
            <a class="ln-btn ln-btn-gold ln-btn-lg" href="${ctx}/register">Get started — it's free &rarr;</a>
            <a class="ln-btn ln-btn-ghost ln-btn-lg" href="${ctx}/login">Log in</a>
        </div>

        <dl class="ln-hero-stats">
            <div class="ln-stat">
                <dt class="ln-stat-num brand-font">${managersCount}</dt>
                <dd class="ln-stat-label">Active managers</dd>
            </div>
            <div class="ln-stat">
                <dt class="ln-stat-num brand-font">${publicLeaguesTotal}</dt>
                <dd class="ln-stat-label">Public leagues</dd>
            </div>
            <div class="ln-stat">
                <dt class="ln-stat-num brand-font">${roundsPerSeason}</dt>
                <dd class="ln-stat-label">Rounds a season</dd>
            </div>
        </dl>
    </div>
</section>

<%-- ============================ Features ============================ --%>
<section class="ln-band" id="how-it-works">
    <div class="ln-container">
        <p class="ln-section-eyebrow">How it works</p>
        <h2 class="ln-section-title brand-font">Everything you need to run your season</h2>

        <div class="ln-feature-grid">
            <article class="ln-feature">
                <span class="ln-feature-icon" aria-hidden="true">
                    <svg width="22" height="22" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.7" stroke-linecap="round" stroke-linejoin="round"><path d="M16 21v-2a4 4 0 0 0-4-4H6a4 4 0 0 0-4 4v2"/><circle cx="9" cy="7" r="4"/><path d="M22 21v-2a4 4 0 0 0-3-3.87"/><path d="M16 3.13a4 4 0 0 1 0 7.75"/></svg>
                </span>
                <h3 class="ln-feature-title">Draft your squad</h3>
                <p class="ln-feature-text">Pick 20 players within a fixed budget, balancing forwards and backs to build a squad that scores.</p>
            </article>
            <article class="ln-feature">
                <span class="ln-feature-icon" aria-hidden="true">
                    <svg width="22" height="22" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.7" stroke-linecap="round" stroke-linejoin="round"><path d="M17 3l4 4-4 4"/><path d="M21 7H8"/><path d="M7 21l-4-4 4-4"/><path d="M3 17h13"/></svg>
                </span>
                <h3 class="ln-feature-title">Make transfers</h3>
                <p class="ln-feature-text">Swap players between rounds as form and fixtures shift — manage your budget and spend transfers wisely.</p>
            </article>
            <article class="ln-feature">
                <span class="ln-feature-icon" aria-hidden="true">
                    <svg width="22" height="22" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.7" stroke-linecap="round" stroke-linejoin="round"><path d="M17 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2"/><circle cx="9" cy="7" r="4"/><path d="M23 21v-2a4 4 0 0 0-3-3.87"/><path d="M16 3.13a4 4 0 0 1 0 7.75"/></svg>
                </span>
                <h3 class="ln-feature-title">Join leagues</h3>
                <p class="ln-feature-text">Compete in public leagues or start a private one with an invite code and take on friends head-to-head.</p>
            </article>
            <article class="ln-feature">
                <span class="ln-feature-icon" aria-hidden="true">
                    <svg width="22" height="22" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.7" stroke-linecap="round" stroke-linejoin="round"><path d="M3 3v18h18"/><path d="M18 17V9"/><path d="M13 17V5"/><path d="M8 17v-3"/></svg>
                </span>
                <h3 class="ln-feature-title">Track every point</h3>
                <p class="ln-feature-text">Every round is simulated and scored automatically, so your points and ranking update as results come in.</p>
            </article>
        </div>
    </div>
</section>

<%-- ============================ Public leagues ============================ --%>
<section class="ln-section" id="public-leagues">
    <div class="ln-container">
        <div class="ln-section-head">
            <div>
                <p class="ln-section-eyebrow">Social proof</p>
                <h2 class="ln-section-title brand-font">Popular public leagues</h2>
            </div>
            <a class="ln-section-link" href="${ctx}/register">Browse all leagues &rarr;</a>
        </div>

        <c:choose>
            <c:when test="${empty publicLeagues}">
                <div class="ln-empty">
                    <h3>New public leagues are on the way</h3>
                    <p>Be among the first to compete — create your team and start one.</p>
                    <a class="ln-btn ln-btn-gold" href="${ctx}/register">Create your team &rarr;</a>
                </div>
            </c:when>
            <c:otherwise>
                <div class="ln-league-grid">
                    <c:forEach var="league" items="${publicLeagues}">
                        <c:set var="lgParts" value="${fn:split(league.leagueName, ' ')}" />
                        <c:set var="spots" value="${league.maxMembers - league.memberCount}" />
                        <article class="ln-lg-card">
                            <div class="ln-lg-top">
                                <span class="ln-lg-badge" aria-hidden="true">
                                    <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.7" stroke-linecap="round" stroke-linejoin="round"><path d="M6 9H4.5a2.5 2.5 0 0 1 0-5H6"/><path d="M18 9h1.5a2.5 2.5 0 0 0 0-5H18"/><path d="M4 22h16"/><path d="M10 14.66V17c0 .55-.47.98-.97 1.21C7.85 18.75 7 20.24 7 22"/><path d="M14 14.66V17c0 .55.47.98.97 1.21C16.15 18.75 17 20.24 17 22"/><path d="M18 2H6v7a6 6 0 0 0 12 0V2Z"/></svg>
                                </span>
                                <span class="ln-lg-pill">Public</span>
                            </div>
                            <h3 class="ln-lg-name" title="${fn:escapeXml(league.leagueName)}">${fn:escapeXml(league.leagueName)}</h3>
                            <p class="ln-lg-count">${league.memberCount} / ${league.maxMembers} teams</p>
                            <p class="ln-lg-desc">${empty league.description ? 'A public league open to all managers.' : fn:escapeXml(league.description)}</p>
                            <div class="ln-lg-foot">
                                <span class="ln-lg-spots">${spots > 0 ? spots : 0} spots left</span>
                                <a class="ln-btn ln-btn-gold ln-btn-sm" href="${ctx}/register">Join</a>
                            </div>
                        </article>
                    </c:forEach>
                </div>
            </c:otherwise>
        </c:choose>
    </div>
</section>

<%-- ============================ Leaderboard ============================ --%>
<section class="ln-band ln-band-center" id="leaderboard">
    <div class="ln-container ln-narrow">
        <p class="ln-section-eyebrow">The master table</p>
        <h2 class="ln-section-title brand-font">Season ${fn:escapeXml(season)} — top of the table</h2>
        <p class="ln-section-lede">The best managers across every league, ranked by total fantasy points.</p>

        <c:choose>
            <c:when test="${empty leaderboard}">
                <div class="ln-empty">
                    <h3>The season is about to kick off</h3>
                    <p>Rankings appear here once the first round has been scored.</p>
                </div>
            </c:when>
            <c:otherwise>
                <div class="ln-board">
                    <div class="ln-board-head">
                        <span class="ln-col-rank">#</span>
                        <span class="ln-col-team">Team</span>
                        <span class="ln-col-num">Played</span>
                        <span class="ln-col-num">Fantasy pts</span>
                    </div>
                    <c:forEach var="entry" items="${leaderboard}">
                        <c:set var="tParts" value="${fn:split(entry.teamName, ' ')}" />
                        <c:set var="medalClass" value="${entry.rank == 1 ? 'is-gold-medal' : (entry.rank == 2 ? 'is-silver-medal' : (entry.rank == 3 ? 'is-bronze-medal' : ''))}" />
                        <div class="ln-board-row">
                            <span class="ln-col-rank">
                                <c:choose>
                                    <c:when test="${entry.rank <= 3}"><span class="ln-medal ${medalClass}">${entry.rank}</span></c:when>
                                    <c:otherwise><span class="ln-rank-plain">${entry.rank}</span></c:otherwise>
                                </c:choose>
                            </span>
                            <span class="ln-col-team">
                                <span class="ln-team-avatar" aria-hidden="true"><c:if test="${fn:length(tParts) > 0}">${fn:toUpperCase(fn:substring(tParts[0], 0, 1))}<c:if test="${fn:length(tParts) > 1}">${fn:toUpperCase(fn:substring(tParts[fn:length(tParts) - 1], 0, 1))}</c:if></c:if></span>
                                <span class="ln-team-id">
                                    <span class="ln-team-name" title="${fn:escapeXml(entry.teamName)}">${fn:escapeXml(entry.teamName)}</span>
                                    <span class="ln-team-owner">@${fn:escapeXml(entry.owner)}</span>
                                </span>
                            </span>
                            <span class="ln-col-num">${entry.matchesPlayed}</span>
                            <span class="ln-col-num is-gold">${entry.totalFantasyPoints}</span>
                        </div>
                    </c:forEach>
                </div>
            </c:otherwise>
        </c:choose>

        <div class="ln-closing">
            <p class="ln-closing-text">Ready to see your name on the table?</p>
            <a class="ln-btn ln-btn-gold ln-btn-lg" href="${ctx}/register">Create your team &rarr;</a>
        </div>
    </div>
</section>

<%-- ============================ Footer ============================ --%>
<footer class="ln-footer">
    <div class="ln-container ln-footer-inner">
        <div class="ln-footer-brand">
            <img class="ln-brand-logo" src="${ctx}/assets/images/Trytons-Logo.png" alt="" aria-hidden="true">
            <span>Fantasy TryTons League &middot; Season ${fn:escapeXml(season)}</span>
        </div>
        <nav class="ln-footer-links" aria-label="Footer">
            <a href="${ctx}/login">Log in</a>
            <a href="${ctx}/register">Register</a>
            <a href="#how-it-works">How it works</a>
        </nav>
    </div>
</footer>

<script>
    // Smooth-scroll the nav anchors to their sections.
    document.querySelectorAll('a[href^="#"]').forEach(function (link) {
        link.addEventListener('click', function (e) {
            var target = document.querySelector(this.getAttribute('href'));
            if (target) {
                e.preventDefault();
                target.scrollIntoView({behavior: 'smooth', block: 'start'});
            }
        });
    });
</script>
</body>
</html>
