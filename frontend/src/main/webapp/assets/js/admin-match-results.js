/* ============================================================================
   TryTons — Admin: Match Result Capture behaviour
   Vanilla JS, display only. Fills the live winner preview from the two score
   inputs and stops the statistic counters going negative. Never changes the
   posted form contract — the server remains the source of truth, and no
   fantasy-point values are computed here.
   ========================================================================== */
(function () {
    "use strict";

    var resultForm = document.getElementById("matchResultForm");
    if (!resultForm) return; // no fixture selected / different page — do nothing

    var teamAScore = document.getElementById("teamAScore");
    var teamBScore = document.getElementById("teamBScore");
    var preview = document.getElementById("scorePreview");
    var previewScore = document.getElementById("scorePreviewScore");
    var previewOutcome = document.getElementById("scorePreviewOutcome");

    /* Winner preview — mirrors how the backend reads the scores, display only. */
    function refreshPreview() {
        if (!preview || !teamAScore || !teamBScore) return;

        var a = parseInt(teamAScore.value, 10);
        var b = parseInt(teamBScore.value, 10);

        // Nothing meaningful to preview until both scores are filled in.
        if (isNaN(a) || isNaN(b)) {
            preview.hidden = true;
            return;
        }

        preview.hidden = false;
        if (previewScore) previewScore.textContent = a + " – " + b;
        if (previewOutcome) {
            previewOutcome.textContent = a === b
                ? "Draw"
                : (a > b ? "Team A wins" : "Team B wins");
        }
    }

    if (teamAScore) teamAScore.addEventListener("input", refreshPreview);
    if (teamBScore) teamBScore.addEventListener("input", refreshPreview);
    refreshPreview();

    /* The inputs already carry min="0"; this also catches typed-in negatives. */
    var counts = document.querySelectorAll('#statCounts input[type="number"]');
    Array.prototype.forEach.call(counts, function (input) {
        input.addEventListener("change", function () {
            if (input.value !== "" && parseInt(input.value, 10) < 0) {
                input.value = "0";
            }
        });
    });

    /* Player select only offers the chosen team's roster — both sides' players
       are rendered into the same <select> (each option tagged with its fantasy
       team id via data-team-id) so this is a client-side filter, not a re-render. */
    var teamSelect = document.getElementById("teamId");
    var playerSelect = document.getElementById("playerId");
    if (teamSelect && playerSelect) {
        var playerOptions = Array.prototype.slice.call(playerSelect.options);

        function refreshPlayerOptions() {
            var teamId = teamSelect.value;
            playerOptions.forEach(function (option) {
                if (!option.value) return; // keep the placeholder
                var matches = option.getAttribute("data-team-id") === teamId;
                option.hidden = !matches;
                option.disabled = !matches;
            });
            if (playerSelect.selectedOptions[0] && playerSelect.selectedOptions[0].disabled) {
                playerSelect.value = "";
            }
        }

        teamSelect.addEventListener("change", refreshPlayerOptions);
        refreshPlayerOptions();
    }
})();
