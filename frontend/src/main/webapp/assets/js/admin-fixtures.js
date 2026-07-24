/* ============================================================================
   TryTons — Admin: Fixture Administration behaviour
   Vanilla JS, display only. The create-fixture form renders every league's
   active team memberships into the two team selects up front (each option
   tagged with its league via data-league-id), so choosing a league is a
   client-side filter rather than a page reload. Never changes the posted
   form contract or the server's authority over what a valid fixture is.
   ========================================================================== */
(function () {
    "use strict";

    var form = document.getElementById("createFixtureForm");
    if (!form) return; // different page — do nothing

    var leagueSelect = document.getElementById("createFixtureLeagueId");
    var teamASelect = document.getElementById("createFixtureTeamAId");
    var teamBSelect = document.getElementById("createFixtureTeamBId");
    if (!leagueSelect || !teamASelect || !teamBSelect) return;

    var teamAOptions = Array.prototype.slice.call(teamASelect.options);
    var teamBOptions = Array.prototype.slice.call(teamBSelect.options);

    // A fixture's two sides must be different teams (chk_fixture_teams_different),
    // so each select also hides whatever the other one currently has chosen.
    function refreshTeamOptions(options, select, otherSelect) {
        var leagueId = leagueSelect.value;
        options.forEach(function (option) {
            if (!option.value) return; // keep the placeholder
            var inLeague = option.getAttribute("data-league-id") === leagueId;
            var isOthersPick = otherSelect && option.value === otherSelect.value;
            var visible = inLeague && !isOthersPick;
            option.hidden = !visible;
            option.disabled = !visible;
        });
        if (select.selectedOptions[0] && select.selectedOptions[0].disabled) {
            select.value = "";
        }
    }

    function refreshBoth() {
        refreshTeamOptions(teamAOptions, teamASelect, teamBSelect);
        refreshTeamOptions(teamBOptions, teamBSelect, teamASelect);
    }

    leagueSelect.addEventListener("change", refreshBoth);
    teamASelect.addEventListener("change", refreshBoth);
    teamBSelect.addEventListener("change", refreshBoth);
    refreshBoth();
})();
