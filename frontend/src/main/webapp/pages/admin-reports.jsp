<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>

<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>System Reports - Fantasy TryTons</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/theme.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/sidebar.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/catalog.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/admin-reports.css">
    <%-- admin-reports.css defines .rtbl2-head/.rtbl2-row as a 3-column CSS grid.
         Bumping to 4 columns here (scoped, so it wins on source order) since a
         "Result" column was added below. If admin-reports.css is later updated
         to size these as 4 columns natively, this override can be removed. --%>
    <style>
        .arep-page .rtbl2-head,
        .arep-page .rtbl2-row {
            grid-template-columns: repeat(4, 1fr);
        }
        /* Filter fields are shown/hidden per report type via JS */
        .arep-field[data-filter-field] {
            display: none;
        }
        .arep-field[data-filter-field].is-visible {
            display: block;
        }
        .rtbl2-result-actions {
            display: flex;
            gap: 10px;
            margin-top: 6px;
        }
        .rtbl2-result-actions a {
            font-size: .78rem;
            font-weight: 600;
            text-decoration: underline;
            color: var(--silver-bright, #eae4d6);
            cursor: pointer;
        }
        .rtbl2-result-summary {
            color: var(--silver-dim, #9aa79c);
            font-size: .82rem;
        }
    </style>
</head>
<body class="catalog-page arep-page">

<c:set var="activeNav" value="admin-reports" scope="request" />
<%@ include file="/WEB-INF/jspf/sidebar.jspf" %>

<main class="catalog-main" id="adminReports">
    <div class="catalog-content">

        <header class="catalog-header">
            <div>
                <p class="catalog-eyebrow">Administration</p>
                <h1 class="brand-font">System Reports</h1>
            </div>
        </header>

        <c:if test="${not empty success}">
            <p class="arep-alert arep-alert-success" role="status"><c:out value="${success}" /></p>
        </c:if>
        <c:if test="${not empty error}">
            <p class="arep-alert arep-alert-error" role="alert"><c:out value="${error}" /></p>
        </c:if>

        <%-- ---------- Generated reports + generate form ---------- --%>
        <div class="arep-grid">

            <section id="reportList">
                <div class="arep-section-head">
                    <h2 class="arep-section-title">Previously generated reports</h2>
                    <span class="arep-rule-line"></span>
                    <span class="arep-count">${fn:length(reports)} report${fn:length(reports) == 1 ? '' : 's'}</span>
                </div>

                <c:choose>
                    <c:when test="${empty reports}">
                        <p class="arep-empty">No reports have been generated yet.</p>
                    </c:when>
                    <c:otherwise>
                        <div class="rtbl2">
                            <div class="rtbl2-scroll">
                                <div class="rtbl2-head">
                                    <span>Type</span>
                                    <span>Title</span>
                                    <span>Generated at</span>
                                    <span>Result</span>
                                </div>
                                <c:forEach var="report" items="${reports}">
                                    <div class="rtbl2-row">
                                        <span><span class="arep-chip"><c:out value="${report.reportType}" /></span></span>
                                        <span class="rtbl2-title" title="${fn:escapeXml(report.reportTitle)}"><c:out value="${report.reportTitle}" /></span>
                                        <span class="rtbl2-when">${fn:substring(fn:replace(report.generatedAt, 'T', ' '), 0, 16)}</span>
                                        <span class="rtbl2-result">
                                            <c:choose>
                                                <c:when test="${empty report.resultJson}">
                                                    <span class="arep-muted">No data</span>
                                                </c:when>
                                                <c:otherwise>
                                                    <span class="rtbl2-result-summary">
                                                        <c:out value="${fn:length(report.resultJson)} field${fn:length(report.resultJson) == 1 ? '' : 's'}" />
                                                    </span>
                                                </c:otherwise>
                                            </c:choose>
                                            <span class="rtbl2-result-actions">
                                                <a href="${pageContext.request.contextPath}/admin/reports?view=${report.reportId}"
                                                   target="_blank" rel="noopener">View</a>
                                                <a href="${pageContext.request.contextPath}/admin/reports?download=${report.reportId}">Download</a>
                                            </span>
                                        </span>
                                    </div>
                                </c:forEach>
                            </div>
                        </div>
                    </c:otherwise>
                </c:choose>
            </section>

            <section class="arep-panel" id="reportGeneration">
                <h2 class="arep-panel-title">Generate report</h2>
                <form action="${pageContext.request.contextPath}/admin/reports" method="post" id="generateReportForm">
                    <div class="arep-field">
                        <label class="arep-label" for="reportType">Report type</label>
                        <div class="arep-select-wrap">
                            <select class="arep-select" id="reportType" name="reportType" required>
                                <option value="">&mdash; Select report type &mdash;</option>
                                <option value="ACTIVE_USERS" data-fields="" ${param.reportType == 'ACTIVE_USERS' ? 'selected' : ''}>Active Users</option>
                                <option value="ACTIVE_LEAGUES" data-fields="" ${param.reportType == 'ACTIVE_LEAGUES' ? 'selected' : ''}>Active Leagues</option>
                                <option value="TOP_FANTASY_TEAMS" data-fields="season,limit" ${param.reportType == 'TOP_FANTASY_TEAMS' ? 'selected' : ''}>Top Fantasy Teams</option>
                                <option value="TOP_RUGBY_PLAYERS" data-fields="limit" ${param.reportType == 'TOP_RUGBY_PLAYERS' ? 'selected' : ''}>Top Rugby Players</option>
                                <option value="MOST_SELECTED_PLAYERS" data-fields="limit" ${param.reportType == 'MOST_SELECTED_PLAYERS' ? 'selected' : ''}>Most Selected Players</option>
                                <option value="UNAVAILABLE_PLAYERS" data-fields="" ${param.reportType == 'UNAVAILABLE_PLAYERS' ? 'selected' : ''}>Unavailable Players</option>
                                <option value="COMPLETED_FIXTURES" data-fields="" ${param.reportType == 'COMPLETED_FIXTURES' ? 'selected' : ''}>Completed Fixtures</option>
                                <option value="FIXTURE_RESULTS" data-fields="" ${param.reportType == 'FIXTURE_RESULTS' ? 'selected' : ''}>Fixture Results</option>
                                <option value="TRANSFER_ACTIVITY" data-fields="roundId" ${param.reportType == 'TRANSFER_ACTIVITY' ? 'selected' : ''}>Transfer Activity</option>
                                <option value="LEAGUE_CHAT_ACTIVITY" data-fields="" ${param.reportType == 'LEAGUE_CHAT_ACTIVITY' ? 'selected' : ''}>League Chat Activity</option>
                                <option value="SYSTEM_ACTIVITY" data-fields="limit" ${param.reportType == 'SYSTEM_ACTIVITY' ? 'selected' : ''}>System Activity</option>
                            </select>
                            <svg class="arep-select-caret" width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.2" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true"><path d="M6 9l6 6 6-6"/></svg>
                        </div>
                    </div>

                    <div class="arep-field">
                        <label class="arep-label" for="reportTitle">Report title</label>
                        <input class="arep-input" type="text" id="reportTitle" name="reportTitle"
                               value="${fn:escapeXml(param.reportTitle)}" placeholder="e.g. Weekly Active Users Report" required>
                    </div>

                    <%-- Filter fields: only the one(s) relevant to the selected report type are shown --%>
                    <div class="arep-field" data-filter-field="season">
                        <label class="arep-label" for="season">Season</label>
                        <input class="arep-input" type="text" id="season" name="season"
                               value="${fn:escapeXml(param.season)}" placeholder="e.g. 2026">
                        <p class="arep-help">Required for Top Fantasy Teams.</p>
                    </div>

                    <div class="arep-field" data-filter-field="limit">
                        <label class="arep-label" for="limit">Limit</label>
                        <input class="arep-input" type="number" min="1" id="limit" name="limit"
                               value="${fn:escapeXml(param.limit)}" placeholder="e.g. 10">
                        <p class="arep-help">Optional. Maximum number of results to include.</p>
                    </div>

                    <div class="arep-field" data-filter-field="roundId">
                        <label class="arep-label" for="roundId">Round ID</label>
                        <input class="arep-input" type="text" id="roundId" name="roundId"
                               value="${fn:escapeXml(param.roundId)}" placeholder="Round UUID">
                        <p class="arep-help">Required for Transfer Activity.</p>
                    </div>

                    <button type="submit" class="btn-gold arep-submit">Generate report</button>
                </form>
            </section>

        </div>

        <%-- ---------- System activity logs ---------- --%>
        <section id="systemLogs">
            <div class="arep-section-head">
                <h2 class="arep-section-title">Recent system activity logs</h2>
                <span class="arep-rule-line"></span>
                <span class="arep-count">${fn:length(logs)} entr${fn:length(logs) == 1 ? 'y' : 'ies'}</span>
            </div>

            <c:choose>
                <c:when test="${empty logs}">
                    <p class="arep-empty">No logs are available.</p>
                </c:when>
                <c:otherwise>
                    <div class="ltbl">
                        <div class="ltbl-scroll">
                            <div class="ltbl-head">
                                <span>Created at</span>
                                <span>Action</span>
                                <span>Entity type</span>
                                <span>Description</span>
                            </div>
                            <c:forEach var="log" items="${logs}">
                                <div class="ltbl-row">
                                    <span class="ltbl-when">${fn:substring(fn:replace(log.createdAt, 'T', ' '), 0, 16)}</span>
                                    <span><span class="arep-chip"><c:out value="${log.actionType}" /></span></span>
                                    <span class="ltbl-entity"><c:out value="${log.entityType}" /></span>
                                    <span class="ltbl-desc" title="${fn:escapeXml(log.description)}"><c:out value="${log.description}" /></span>
                                </div>
                            </c:forEach>
                        </div>
                    </div>
                </c:otherwise>
            </c:choose>
        </section>

    </div>
</main>

<script>
    (function () {
        var select = document.getElementById('reportType');
        var fieldGroups = document.querySelectorAll('[data-filter-field]');

        function applyVisibility() {
            var selectedOption = select.options[select.selectedIndex];
            var activeFields = (selectedOption && selectedOption.getAttribute('data-fields') || '')
                .split(',')
                .map(function (f) { return f.trim(); })
                .filter(Boolean);

            fieldGroups.forEach(function (group) {
                var fieldName = group.getAttribute('data-filter-field');
                var isVisible = activeFields.indexOf(fieldName) !== -1;
                group.classList.toggle('is-visible', isVisible);
                var input = group.querySelector('input');
                if (input) {
                    input.disabled = !isVisible;
                }
            });
        }

        select.addEventListener('change', applyVisibility);
        applyVisibility();
    })();
</script>

</body>
</html>
