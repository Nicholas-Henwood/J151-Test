<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>

<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>Scoring Rule Management - Fantasy TryTons</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/admin-scoring-rules.css">
</head>
<body class="admin-scoring-rules">
<%@ include file="/WEB-INF/jspf/navigation.jspf" %>

<main id="adminScoringRules">

    <h1>Scoring Rule Management</h1>
    <p class="page-intro">
        Manage the fantasy points awarded, or deducted, for each match event, scoped per season.
    </p>

    <c:if test="${not empty success}">
        <p class="success-message" role="status"><c:out value="${success}" /></p>
    </c:if>

    <c:if test="${not empty error}">
        <p class="error-message" role="alert"><c:out value="${error}" /></p>
    </c:if>

    <c:if test="${not empty rulesError}">
        <p class="error-message" role="alert"><c:out value="${rulesError}" /></p>
    </c:if>

    <section id="seasonSelection">
        <h2>Season</h2>
        <form action="${pageContext.request.contextPath}/admin/scoring-rules" method="get" id="seasonSelectForm">
            <div class="field">
                <label for="seasonSelect">Season</label>
                <input type="text" id="seasonSelect" name="season" value="${fn:escapeXml(selectedSeason)}" required>
            </div>
            <button type="submit">Load season</button>
        </form>
    </section>

    <section id="scoringRuleListSection">
        <h2>Scoring Rules for <c:out value="${selectedSeason}" /></h2>
        <c:choose>
            <c:when test="${empty scoringRules}">
                <p id="scoringRulesEmptyState">No scoring rules exist for this season yet.</p>
            </c:when>
            <c:otherwise>
                <table id="scoringRulesTable">
                    <thead>
                    <tr>
                        <th>Event type</th>
                        <th>Points awarded</th>
                        <th>Deduction</th>
                        <th>Active</th>
                        <th>Description</th>
                        <th></th>
                    </tr>
                    </thead>
                    <tbody>
                    <c:forEach var="rule" items="${scoringRules}">
                        <tr>
                            <td><c:out value="${rule.eventType}" /></td>
                            <td><c:out value="${rule.pointsAwarded}" /></td>
                            <td>${rule.isDeduction ? 'Yes' : 'No'}</td>
                            <td>${rule.active ? 'Yes' : 'No'}</td>
                            <td><c:out value="${rule.description}" /></td>
                            <td>
                                <a href="${pageContext.request.contextPath}/admin/scoring-rules?season=${fn:escapeXml(selectedSeason)}&amp;ruleId=${rule.ruleId}">Edit</a>
                            </td>
                        </tr>
                    </c:forEach>
                    </tbody>
                </table>
            </c:otherwise>
        </c:choose>
    </section>

    <section id="scoringRuleFormSection">
        <c:choose>
            <c:when test="${not empty editingRule}">
                <h2>Edit Scoring Rule</h2>
            </c:when>
            <c:otherwise>
                <h2>Create Scoring Rule</h2>
            </c:otherwise>
        </c:choose>

        <form method="post" action="${pageContext.request.contextPath}/admin/scoring-rules" id="scoringRuleForm">
            <c:if test="${not empty editingRule}">
                <input type="hidden" name="ruleId" value="${editingRule.ruleId}">
            </c:if>

            <div class="field">
                <label for="eventType">Event type</label>
                <input type="text" id="eventType" name="eventType"
                        value="${not empty editingRule ? fn:escapeXml(editingRule.eventType) : ''}" required>
            </div>

            <div class="field">
                <label for="pointsAwarded">Points awarded</label>
                <input type="number" id="pointsAwarded" name="pointsAwarded" step="1"
                        value="${not empty editingRule ? editingRule.pointsAwarded : ''}" required>
            </div>

            <div class="field">
                <label for="season">Season</label>
                <input type="text" id="season" name="season"
                        value="${not empty editingRule ? fn:escapeXml(editingRule.season) : fn:escapeXml(selectedSeason)}" required>
            </div>

            <div class="field">
                <label for="description">Description</label>
                <input type="text" id="description" name="description"
                        value="${not empty editingRule ? fn:escapeXml(editingRule.description) : ''}">
            </div>

            <div class="field field-checkbox">
                <label for="active">
                    <input type="checkbox" id="active" name="active"
                            ${(empty editingRule) or editingRule.active ? 'checked' : ''}>
                    Active
                </label>
            </div>

            <div class="field field-checkbox">
                <label for="isDeduction">
                    <input type="checkbox" id="isDeduction" name="isDeduction"
                            ${(not empty editingRule) and editingRule.isDeduction ? 'checked' : ''}>
                    Deduction
                </label>
            </div>

            <button type="submit" name="submit" value="save-scoring-rule">
                <c:choose>
                    <c:when test="${not empty editingRule}">Update scoring rule</c:when>
                    <c:otherwise>Create scoring rule</c:otherwise>
                </c:choose>
            </button>

            <c:if test="${not empty editingRule}">
                <a href="${pageContext.request.contextPath}/admin/scoring-rules?season=${fn:escapeXml(selectedSeason)}">Cancel edit</a>
            </c:if>
        </form>
    </section>

</main>
</body>
</html>
