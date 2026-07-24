<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>

<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Simulation Settings - Fantasy TryTons</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/theme.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/sidebar.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/catalog.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/admin-simulation.css">
</head>
<body class="catalog-page asim-page">

<c:set var="activeNav" value="admin-simulation" scope="request" />
<%@ include file="/WEB-INF/jspf/sidebar.jspf" %>

<main class="catalog-main" id="adminSimulation">
    <div class="catalog-content">

        <header class="catalog-header">
            <div>
                <p class="catalog-eyebrow">Administration</p>
                <h1 class="brand-font">Simulation Settings</h1>
                <p class="asim-intro">
                    Control the weighting used when generating per-player match statistics, and trigger admin-controlled
                    resimulations for individual fixtures.
                </p>
            </div>
        </header>

        <c:if test="${not empty success}">
            <p class="asim-alert asim-alert-success" role="status"><c:out value="${success}" /></p>
        </c:if>
        <c:if test="${not empty error}">
            <p class="asim-alert asim-alert-error" role="alert"><c:out value="${error}" /></p>
        </c:if>
        <c:if test="${not empty activeSettingError}">
            <p class="asim-alert asim-alert-error" role="alert"><c:out value="${activeSettingError}" /></p>
        </c:if>

        <%-- ---------- Active simulation settings ---------- --%>
        <section id="activeSettingSection">
            <div class="asim-section-head">
                <h2 class="asim-section-title">Active simulation settings</h2>
                <span class="asim-rule-line"></span>
            </div>

            <c:choose>
                <c:when test="${empty activeSetting}">
                    <p class="asim-empty" id="activeSettingEmptyState">There are no active simulation settings configured.</p>
                </c:when>
                <c:otherwise>
                    <div class="asim-cells" id="activeSettingTable">
                        <div class="asim-cell">
                            <span class="asim-cell-label">Season</span>
                            <span class="asim-cell-value is-gold"><c:out value="${activeSetting.season}" /></span>
                        </div>
                        <div class="asim-cell">
                            <span class="asim-cell-label">Player ability</span>
                            <span class="asim-cell-value"><fmt:formatNumber value="${activeSetting.playerAbilityWeight}" minFractionDigits="2" maxFractionDigits="2" /></span>
                        </div>
                        <div class="asim-cell">
                            <span class="asim-cell-label">Player form</span>
                            <span class="asim-cell-value"><fmt:formatNumber value="${activeSetting.playerFormWeight}" minFractionDigits="2" maxFractionDigits="2" /></span>
                        </div>
                        <div class="asim-cell">
                            <span class="asim-cell-label">Team balance</span>
                            <span class="asim-cell-value"><fmt:formatNumber value="${activeSetting.teamBalanceWeight}" minFractionDigits="2" maxFractionDigits="2" /></span>
                        </div>
                        <div class="asim-cell">
                            <span class="asim-cell-label">Random variation</span>
                            <span class="asim-cell-value"><fmt:formatNumber value="${activeSetting.randomVariationWeight}" minFractionDigits="2" maxFractionDigits="2" /></span>
                        </div>
                        <div class="asim-cell">
                            <span class="asim-cell-label">Max resimulations</span>
                            <span class="asim-cell-value"><c:out value="${activeSetting.maxResimulations}" /></span>
                        </div>
                        <div class="asim-cell">
                            <span class="asim-cell-label">Admin approval</span>
                            <span class="asim-cell-value"><span class="asim-flag ${activeSetting.requireAdminApproval ? 'is-yes' : 'is-no'}">${activeSetting.requireAdminApproval ? 'Yes' : 'No'}</span></span>
                        </div>
                        <div class="asim-cell">
                            <span class="asim-cell-label">Allows resim.</span>
                            <span class="asim-cell-value"><span class="asim-flag ${activeSetting.allowResimulation ? 'is-yes' : 'is-no'}">${activeSetting.allowResimulation ? 'Yes' : 'No'}</span></span>
                        </div>
                        <div class="asim-cell">
                            <span class="asim-cell-label">Active</span>
                            <span class="asim-cell-value"><span class="asim-flag ${activeSetting.isActive ? 'is-yes' : 'is-no'}">${activeSetting.isActive ? 'Yes' : 'No'}</span></span>
                        </div>
                    </div>
                </c:otherwise>
            </c:choose>
        </section>

        <%-- ---------- All settings + settings form ---------- --%>
        <div class="asim-grid">

            <section id="allSettingsSection">
                <div class="asim-section-head">
                    <h2 class="asim-section-title">All simulation settings</h2>
                    <span class="asim-rule-line"></span>
                    <span class="asim-count">${fn:length(simulationSettings)} setting${fn:length(simulationSettings) == 1 ? '' : 's'}</span>
                </div>

                <c:choose>
                    <c:when test="${empty simulationSettings}">
                        <p class="asim-empty" id="allSettingsEmptyState">No simulation settings have been created yet.</p>
                    </c:when>
                    <c:otherwise>
                        <div class="stbl2" id="allSettingsTable">
                            <div class="stbl2-head">
                                <span>Season</span>
                                <span class="stbl2-c">Active</span>
                                <span aria-hidden="true"></span>
                            </div>
                            <c:forEach var="setting" items="${simulationSettings}">
                                <div class="stbl2-row">
                                    <span class="stbl2-season"><c:out value="${setting.season}" /></span>
                                    <span class="stbl2-c"><span class="asim-flag ${setting.isActive ? 'is-yes' : 'is-no'}">${setting.isActive ? 'Yes' : 'No'}</span></span>
                                    <span class="stbl2-action">
                                        <%-- Fills the settings form client-side; unchanged from before. --%>
                                        <a class="stbl2-edit" href="#simulationSettingForm"
                                           onclick="return trytonsFillSettingsForm('${setting.settingsId}','${fn:escapeXml(setting.season)}','${setting.playerAbilityWeight}','${setting.playerFormWeight}','${setting.teamBalanceWeight}','${setting.randomVariationWeight}',${setting.requireAdminApproval},${setting.allowResimulation},'${setting.maxResimulations}',${setting.isActive});">Edit</a>
                                    </span>
                                </div>
                            </c:forEach>
                        </div>
                    </c:otherwise>
                </c:choose>
            </section>

            <section class="asim-panel" id="settingsFormSection">
                <%-- Create is the default, always-available state; Update only replaces it
                     once an "Edit" link has actually loaded a row (trytonsFillSettingsForm
                     below) — the two are visually distinct rather than one ambiguous panel
                     that silently repurposes itself between create and edit. --%>
                <div class="asim-panel-head" id="createSettingsHead">
                    <h2 class="asim-panel-title" id="simulationSettingForm">Create settings</h2>
                </div>
                <div class="asim-panel-head" id="updateSettingsHead" hidden>
                    <h2 class="asim-panel-title">Update settings &mdash; <span id="updateSettingsSeason"></span></h2>
                    <button type="button" class="asim-ghost asim-form-reset" onclick="return trytonsResetSettingsForm();">Cancel edit</button>
                </div>

                <form method="post" action="${pageContext.request.contextPath}/admin/simulation" id="simulationSettingsForm">
                    <input type="hidden" name="action" value="saveSettings">
                    <input type="hidden" id="settingsId" name="settingsId" value="">

                    <div class="asim-field">
                        <label class="asim-label" for="settingsSeason">Season</label>
                        <input class="asim-input" type="text" id="settingsSeason" name="season" placeholder="e.g. 2025/26" required>
                    </div>

                    <div class="asim-row-2">
                        <div class="asim-field">
                            <label class="asim-label" for="playerAbilityWeight">Player ability</label>
                            <input class="asim-input" type="number" step="0.01" id="playerAbilityWeight" name="playerAbilityWeight" required>
                        </div>
                        <div class="asim-field">
                            <label class="asim-label" for="playerFormWeight">Player form</label>
                            <input class="asim-input" type="number" step="0.01" id="playerFormWeight" name="playerFormWeight" required>
                        </div>
                        <div class="asim-field">
                            <label class="asim-label" for="teamBalanceWeight">Team balance</label>
                            <input class="asim-input" type="number" step="0.01" id="teamBalanceWeight" name="teamBalanceWeight" required>
                        </div>
                        <div class="asim-field">
                            <label class="asim-label" for="randomVariationWeight">Random variation</label>
                            <input class="asim-input" type="number" step="0.01" id="randomVariationWeight" name="randomVariationWeight" required>
                        </div>
                    </div>

                    <div class="asim-field">
                        <label class="asim-label" for="maxResimulations">Max resimulations</label>
                        <input class="asim-input" type="number" step="1" min="0" id="maxResimulations" name="maxResimulations" required>
                    </div>

                    <div class="asim-checks">
                        <label class="asim-check" for="requireAdminApproval">
                            <input type="checkbox" id="requireAdminApproval" name="requireAdminApproval">
                            <span>Require admin approval</span>
                        </label>
                        <label class="asim-check" for="allowResimulation">
                            <input type="checkbox" id="allowResimulation" name="allowResimulation">
                            <span>Allow resimulation</span>
                        </label>
                        <label class="asim-check" for="isActive">
                            <input type="checkbox" id="isActive" name="isActive" checked>
                            <span>Active</span>
                        </label>
                    </div>

                    <button type="submit" class="btn-gold asim-submit" id="settingsSubmitBtn">Save simulation settings</button>
                </form>
            </section>

        </div>

        <%-- ---------- Controlled resimulation ---------- --%>
        <section id="resimulationSection">
            <div class="asim-section-head">
                <h2 class="asim-section-title">Controlled resimulation</h2>
                <span class="asim-rule-line"></span>
            </div>

            <%-- No static disclaimer here: the backend (ControlledResimulationServiceImpl) is
                 fully implemented, and a rejected trigger already surfaces its specific
                 reason (e.g. "The round has not reached its lock deadline.") through the
                 ${error} alert above — that is the only explanation an admin needs, and
                 only appears when something actually goes wrong. --%>
            <div class="asim-grid asim-grid-even">

                <section class="asim-panel">
                    <h3 class="asim-panel-title">Resimulation history</h3>

                    <form method="get" action="${pageContext.request.contextPath}/admin/simulation#resimulationSection" id="resimulationHistoryForm" class="asim-inline-form">
                        <div class="asim-field asim-field-grow">
                            <label class="asim-label" for="historyFixtureId">Fixture</label>
                            <select class="asim-input" id="historyFixtureId" name="fixtureId" required>
                                <option value="">&mdash; Select fixture &mdash;</option>
                                <c:forEach var="fixture" items="${fixtures}">
                                    <option value="${fixture.fixtureId}" ${fixture.fixtureId eq selectedFixtureId ? 'selected' : ''}>
                                        <c:out value="${fixture.teamAName}" /> vs <c:out value="${fixture.teamBName}" /> &mdash; ${fixture.fixtureDate}
                                    </option>
                                </c:forEach>
                            </select>
                        </div>
                        <button type="submit" class="asim-ghost">View history</button>
                    </form>

                    <c:if test="${not empty selectedFixtureId}">
                        <c:choose>
                            <c:when test="${empty resimulations}">
                                <p class="asim-empty asim-empty-sm" id="resimulationsEmptyState">No resimulations recorded for this fixture yet.</p>
                            </c:when>
                            <c:otherwise>
                                <div class="rtbl" id="resimulationsTable">
                                    <div class="rtbl-scroll">
                                        <div class="rtbl-head">
                                            <span class="rtbl-c">Run #</span>
                                            <span>Reason</span>
                                            <span class="rtbl-c">Current</span>
                                            <span class="rtbl-c">Approved</span>
                                            <span>Resimulated at</span>
                                        </div>
                                        <c:forEach var="resimulation" items="${resimulations}">
                                            <div class="rtbl-row">
                                                <span class="rtbl-c rtbl-run"><c:out value="${resimulation.simulationRunNumber}" /></span>
                                                <span class="rtbl-reason" title="${fn:escapeXml(resimulation.resimulationReason)}"><c:out value="${resimulation.resimulationReason}" /></span>
                                                <span class="rtbl-c"><span class="asim-flag ${resimulation.current ? 'is-yes' : 'is-no'}">${resimulation.current ? 'Yes' : 'No'}</span></span>
                                                <span class="rtbl-c"><span class="asim-flag ${resimulation.approved ? 'is-yes' : 'is-no'}">${resimulation.approved ? 'Yes' : 'No'}</span></span>
                                                <span class="rtbl-when">${fn:substring(fn:replace(resimulation.resimulatedAt, 'T', ' '), 0, 16)}</span>
                                            </div>
                                        </c:forEach>
                                    </div>
                                </div>
                            </c:otherwise>
                        </c:choose>
                    </c:if>
                </section>

                <section class="asim-panel">
                    <h3 class="asim-panel-title">Trigger resimulation</h3>

                    <form method="post" action="${pageContext.request.contextPath}/admin/simulation#resimulationSection" id="resimulationTriggerForm">
                        <input type="hidden" name="action" value="resimulate">

                        <div class="asim-field">
                            <label class="asim-label" for="resimulateFixtureId">Fixture</label>
                            <select class="asim-input" id="resimulateFixtureId" name="fixtureId" required>
                                <option value="">&mdash; Select fixture &mdash;</option>
                                <c:forEach var="fixture" items="${fixtures}">
                                    <option value="${fixture.fixtureId}" ${fixture.fixtureId eq selectedFixtureId ? 'selected' : ''}>
                                        <c:out value="${fixture.teamAName}" /> vs <c:out value="${fixture.teamBName}" /> &mdash; ${fixture.fixtureDate}
                                    </option>
                                </c:forEach>
                            </select>
                        </div>

                        <div class="asim-field">
                            <label class="asim-label" for="resimulationReason">Reason</label>
                            <input class="asim-input" type="text" id="resimulationReason" name="resimulationReason" placeholder="e.g. correction after review" required>
                        </div>

                        <button type="submit" class="btn-gold asim-submit">Trigger resimulation</button>
                    </form>
                </section>

            </div>
        </section>

    </div>
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
        document.getElementById("createSettingsHead").hidden = true;
        document.getElementById("updateSettingsHead").hidden = false;
        document.getElementById("updateSettingsSeason").textContent = season;
        document.getElementById("settingsSubmitBtn").textContent = "Save changes";
        return false;
    }

    // The only way back to the create form once a row has been loaded for editing —
    // without this, admins had to reload the whole page to create a new setting.
    function trytonsResetSettingsForm() {
        document.getElementById("simulationSettingsForm").reset();
        document.getElementById("settingsId").value = "";
        document.getElementById("createSettingsHead").hidden = false;
        document.getElementById("updateSettingsHead").hidden = true;
        document.getElementById("settingsSubmitBtn").textContent = "Save simulation settings";
        return false;
    }
</script>
</body>
</html>
