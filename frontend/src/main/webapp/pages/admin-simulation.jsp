<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>

<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>Simulation Settings - Fantasy TryTons</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/admin-simulation.css">
</head>
<body class="admin-simulation">
<%@ include file="/WEB-INF/jspf/navigation.jspf" %>

<main id="adminSimulation">

    <h1>Simulation Settings</h1>
    <p class="page-intro">
        Control the weighting used when generating per-player match statistics, and trigger admin-controlled
        resimulations for individual fixtures.
    </p>

    <c:if test="${not empty success}">
        <p class="success-message" role="status"><c:out value="${success}" /></p>
    </c:if>

    <c:if test="${not empty error}">
        <p class="error-message" role="alert"><c:out value="${error}" /></p>
    </c:if>

    <c:if test="${not empty activeSettingError}">
        <p class="error-message" role="alert"><c:out value="${activeSettingError}" /></p>
    </c:if>

    <section id="activeSettingSection">
        <h2>Active Simulation Settings</h2>
        <c:choose>
            <c:when test="${empty activeSetting}">
                <p id="activeSettingEmptyState">There are no active simulation settings configured.</p>
            </c:when>
            <c:otherwise>
                <table id="activeSettingTable">
                    <tbody>
                    <tr><th scope="row">Season</th><td><c:out value="${activeSetting.season}" /></td></tr>
                    <tr><th scope="row">Player ability weight</th><td><c:out value="${activeSetting.playerAbilityWeight}" /></td></tr>
                    <tr><th scope="row">Player form weight</th><td><c:out value="${activeSetting.playerFormWeight}" /></td></tr>
                    <tr><th scope="row">Team balance weight</th><td><c:out value="${activeSetting.teamBalanceWeight}" /></td></tr>
                    <tr><th scope="row">Random variation weight</th><td><c:out value="${activeSetting.randomVariationWeight}" /></td></tr>
                    <tr><th scope="row">Requires admin approval</th><td>${activeSetting.requireAdminApproval ? 'Yes' : 'No'}</td></tr>
                    <tr><th scope="row">Allows resimulation</th><td>${activeSetting.allowResimulation ? 'Yes' : 'No'}</td></tr>
                    <tr><th scope="row">Max resimulations</th><td><c:out value="${activeSetting.maxResimulations}" /></td></tr>
                    <tr><th scope="row">Active</th><td>${activeSetting.isActive ? 'Yes' : 'No'}</td></tr>
                    </tbody>
                </table>
            </c:otherwise>
        </c:choose>
    </section>

    <section id="allSettingsSection">
        <h2>All Simulation Settings</h2>
        <c:choose>
            <c:when test="${empty simulationSettings}">
                <p id="allSettingsEmptyState">No simulation settings have been created yet.</p>
            </c:when>
            <c:otherwise>
                <table id="allSettingsTable">
                    <thead>
                    <tr>
                        <th>Season</th>
                        <th>Active</th>
                        <th></th>
                    </tr>
                    </thead>
                    <tbody>
                    <c:forEach var="setting" items="${simulationSettings}">
                        <tr>
                            <td><c:out value="${setting.season}" /></td>
                            <td>${setting.isActive ? 'Yes' : 'No'}</td>
                            <td>
                                <a href="#simulationSettingForm"
                                    onclick="return trytonsFillSettingsForm('${setting.settingsId}','${fn:escapeXml(setting.season)}','${setting.playerAbilityWeight}','${setting.playerFormWeight}','${setting.teamBalanceWeight}','${setting.randomVariationWeight}',${setting.requireAdminApproval},${setting.allowResimulation},'${setting.maxResimulations}',${setting.isActive});">Edit</a>
                            </td>
                        </tr>
                    </c:forEach>
                    </tbody>
                </table>
            </c:otherwise>
        </c:choose>
    </section>

    <section id="settingsFormSection">
        <h2 id="simulationSettingForm">Create / Update Simulation Settings</h2>
        <form method="post" action="${pageContext.request.contextPath}/admin/simulation" id="simulationSettingsForm">
            <input type="hidden" name="action" value="saveSettings">
            <input type="hidden" id="settingsId" name="settingsId" value="">

            <div class="field">
                <label for="settingsSeason">Season</label>
                <input type="text" id="settingsSeason" name="season" required>
            </div>

            <div class="field">
                <label for="playerAbilityWeight">Player ability weight</label>
                <input type="number" step="0.01" id="playerAbilityWeight" name="playerAbilityWeight" required>
            </div>

            <div class="field">
                <label for="playerFormWeight">Player form weight</label>
                <input type="number" step="0.01" id="playerFormWeight" name="playerFormWeight" required>
            </div>

            <div class="field">
                <label for="teamBalanceWeight">Team balance weight</label>
                <input type="number" step="0.01" id="teamBalanceWeight" name="teamBalanceWeight" required>
            </div>

            <div class="field">
                <label for="randomVariationWeight">Random variation weight</label>
                <input type="number" step="0.01" id="randomVariationWeight" name="randomVariationWeight" required>
            </div>

            <div class="field">
                <label for="maxResimulations">Max resimulations</label>
                <input type="number" step="1" min="0" id="maxResimulations" name="maxResimulations" required>
            </div>

            <div class="field field-checkbox">
                <label for="requireAdminApproval">
                    <input type="checkbox" id="requireAdminApproval" name="requireAdminApproval">
                    Require admin approval
                </label>
            </div>

            <div class="field field-checkbox">
                <label for="allowResimulation">
                    <input type="checkbox" id="allowResimulation" name="allowResimulation">
                    Allow resimulation
                </label>
            </div>

            <div class="field field-checkbox">
                <label for="isActive">
                    <input type="checkbox" id="isActive" name="isActive" checked>
                    Active
                </label>
            </div>

            <button type="submit">Save simulation settings</button>
        </form>
    </section>

    <section id="resimulationSection">
        <h2>Controlled Resimulation</h2>
        <p class="hint">
            The controlled resimulation backend service is still a stub, so triggering a resimulation or
            loading a fixture's history may currently return no result. The form below is wired and ready
            for when that service lands.
        </p>

        <form method="get" action="${pageContext.request.contextPath}/admin/simulation" id="resimulationHistoryForm">
            <div class="field">
                <label for="historyFixtureId">Fixture ID</label>
                <input type="text" id="historyFixtureId" name="fixtureId" value="${fn:escapeXml(selectedFixtureId)}" required>
            </div>
            <button type="submit">View resimulation history</button>
        </form>

        <c:if test="${not empty selectedFixtureId}">
            <c:choose>
                <c:when test="${empty resimulations}">
                    <p id="resimulationsEmptyState">No resimulations recorded for this fixture yet.</p>
                </c:when>
                <c:otherwise>
                    <table id="resimulationsTable">
                        <thead>
                        <tr>
                            <th>Run #</th>
                            <th>Reason</th>
                            <th>Current</th>
                            <th>Approved</th>
                            <th>Resimulated at</th>
                        </tr>
                        </thead>
                        <tbody>
                        <c:forEach var="resimulation" items="${resimulations}">
                            <tr>
                                <td><c:out value="${resimulation.simulationRunNumber}" /></td>
                                <td><c:out value="${resimulation.resimulationReason}" /></td>
                                <td>${resimulation.current ? 'Yes' : 'No'}</td>
                                <td>${resimulation.approved ? 'Yes' : 'No'}</td>
                                <td><c:out value="${resimulation.resimulatedAt}" /></td>
                            </tr>
                        </c:forEach>
                        </tbody>
                    </table>
                </c:otherwise>
            </c:choose>
        </c:if>

        <form method="post" action="${pageContext.request.contextPath}/admin/simulation" id="resimulationTriggerForm">
            <input type="hidden" name="action" value="resimulate">

            <div class="field">
                <label for="resimulateFixtureId">Fixture ID</label>
                <input type="text" id="resimulateFixtureId" name="fixtureId" value="${fn:escapeXml(selectedFixtureId)}" required>
            </div>

            <div class="field">
                <label for="resimulationReason">Reason</label>
                <input type="text" id="resimulationReason" name="resimulationReason" placeholder="e.g. correction after review" required>
            </div>

            <button type="submit">Trigger resimulation</button>
        </form>
    </section>

</main>
<script>
    function trytonsFillSettingsForm(settingsId, season, playerAbilityWeight, playerFormWeight, teamBalanceWeight, randomVariationWeight, requireAdminApproval, allowResimulation, maxResimulations, isActive) {
        document.getElementById("settingsId").value = settingsId;
        document.getElementById("settingsSeason").value = season;
        document.getElementById("playerAbilityWeight").value = playerAbilityWeight;
        document.getElementById("playerFormWeight").value = playerFormWeight;
        document.getElementById("teamBalanceWeight").value = teamBalanceWeight;
        document.getElementById("randomVariationWeight").value = randomVariationWeight;
        document.getElementById("maxResimulations").value = maxResimulations;
        document.getElementById("requireAdminApproval").checked = requireAdminApproval;
        document.getElementById("allowResimulation").checked = allowResimulation;
        document.getElementById("isActive").checked = isActive;
        return false;
    }
</script>
</body>
</html>
