<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>

<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <title>System Reports - Fantasy TryTons</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/admin-reports.css">
</head>
<body class="admin-reports">
<%@ include file="/WEB-INF/jspf/navigation.jspf" %>

<main id="adminReports">

    <h1>System Reports</h1>

    <c:if test="${not empty success}">
            <p class="success-message" role="status">
                <c:out value="${success}"/>
            </p>
        </c:if>
        <c:if test="${not empty error}">
            <p class="error-message" role="alert">
                <c:out value="${error}"/>
            </p>
        </c:if>


        <section id="reportGeneration">
            <h2>Generate New System Report</h2>
            <form action="${pageContext.request.contextPath}/admin/reports" method="post" id="generateReportForm">
                <div class="field">
                    <label for="reportType">Report Type</label>
                    <select id="reportType" name="reportType" required>
                        <option value="">-- Select Report Type --</option>

                                            <option value="ACTIVE_USERS"
                                                <c:if test="${param.reportType == 'ACTIVE_USERS'}">selected</c:if>>
                                                Active Users
                                            </option>

                                            <option value="ACTIVE_LEAGUES"
                                                <c:if test="${param.reportType == 'ACTIVE_LEAGUES'}">selected</c:if>>
                                                Active Leagues
                                            </option>

                                            <option value="TOP_FANTASY_TEAMS"
                                                <c:if test="${param.reportType == 'TOP_FANTASY_TEAMS'}">selected</c:if>>
                                                Top Fantasy Teams
                                            </option>

                                            <option value="TOP_RUGBY_PLAYERS"
                                                <c:if test="${param.reportType == 'TOP_RUGBY_PLAYERS'}">selected</c:if>>
                                                Top Rugby Players
                                            </option>

                                            <option value="MOST_SELECTED_PLAYERS"
                                                <c:if test="${param.reportType == 'MOST_SELECTED_PLAYERS'}">selected</c:if>>
                                                Most Selected Players
                                            </option>

                                            <option value="UNAVAILABLE_PLAYERS"
                                                <c:if test="${param.reportType == 'UNAVAILABLE_PLAYERS'}">selected</c:if>>
                                                Unavailable Players
                                            </option>

                                            <option value="COMPLETED_FIXTURES"
                                                <c:if test="${param.reportType == 'COMPLETED_FIXTURES'}">selected</c:if>>
                                                Completed Fixtures
                                            </option>

                                            <option value="FIXTURE_RESULTS"
                                                <c:if test="${param.reportType == 'FIXTURE_RESULTS'}">selected</c:if>>
                                                Fixture Results
                                            </option>

                                            <option value="TRANSFER_ACTIVITY"
                                                <c:if test="${param.reportType == 'TRANSFER_ACTIVITY'}">selected</c:if>>
                                                Transfer Activity
                                            </option>

                                            <option value="LEAGUE_CHAT_ACTIVITY"
                                                <c:if test="${param.reportType == 'LEAGUE_CHAT_ACTIVITY'}">selected</c:if>>
                                                League Chat Activity
                                            </option>

                                            <option value="SYSTEM_ACTIVITY"
                                                <c:if test="${param.reportType == 'SYSTEM_ACTIVITY'}">selected</c:if>>
                                                System Activity
                                            </option>

                    </select>
                </div>

                <div class="field">
                    <label for="reportTitle">Report Title</label>

                                    <input
                                            type="text"
                                            id="reportTitle"
                                            name="reportTitle"
                                            value="${param.reportTitle}"
                                            placeholder="e.g. Weekly Active Users Report"
                                            required>
                </div>

                <div class="field">
                    <label for="parametersJson">
                                        Parameters (JSON - optional)
                                    </label>

                                    <textarea
                                            id="parametersJson"
                                            name="parametersJson"
                                            rows="4"
                                            placeholder='{"season":2026,"limit":50}'><c:out value="${param.parametersJson}"/></textarea>

                                    <small>
                                        Optional JSON object containing report parameters.
                                    </small>
                </div>

                <button type="submit">Generate Report</button>
            </form>
        </section>
        <section id="reportList">
            <h2>Previously Generated Reports</h2>

            <c:choose>
                <c:when test="${empty reports}">
                    <p>No reports have been generated yet.</p>
                </c:when>
                <c:otherwise>
                    <table id="reportsTable">
                        <thead>
                            <tr>
                                <th>Report Type</th>
                                <th>Report Title</th>
                                <th>Generated At</th>
                                <th>Generated By</th>
                            </tr>
                        </thead>
                        <tbody>
                            <c:forEach var="report" items="${reports}">
                                <tr>
                                    <td>
                                                                    <c:out value="${report.reportType}"/>
                                                                </td>

                                                                <td>
                                                                    <c:out value="${report.reportTitle}"/>
                                                                </td>

                                                                <td>
                                                                    <c:out value="${report.generatedAt}"/>
                                                                </td>

                                                                <td>
                                                                    <c:out value="${report.generatedByAdminUserId}"/>
                                                                </td>
                                </tr>
                            </c:forEach>
                        </tbody>
                    </table>
                </c:otherwise>
            </c:choose>
        </section>

        <section id="systemLogs">
            <h2>Recent System Activity Logs</h2>

            <c:choose>
                <c:when test="${empty logs}">
                    <p>No logs are available.</p>
                </c:when>
                <c:otherwise>
                    <table id="logsTable">
                        <thead>
                            <tr>
                                <th>Created At</th>
                                <th>Action Type</th>
                                <th>Entity Type</th>
                                <th>Entity Id</th>
                                <th>Description</th>
                            </tr>
                        </thead>
                        <tbody>
                            <c:forEach var="log" items="${logs}">
                                <tr>
                                    <td>
                                        <c:out value="${log.createdAt}"/>
                                    </td>
                                    <td>
                                        <c:out value="${log.actionType}"/>
                                    </td>
                                    <td>
                                        <c:out value="${log.entityType}"/>
                                    </td>
                                    <td>
                                        <c:out value="${log.entityId}"/>
                                    </td>
                                    <td>
                                        <c:out value="${log.description}"/>
                                    </td>
                                </tr>
                            </c:forEach>
                        </tbody>
                    </table>
                </c:otherwise>
            </c:choose>
        </section>

</main>
</body>
</html>
