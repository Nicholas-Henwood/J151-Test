/* ============================================================================
   TryTons — Fantasy Rugby | Create Team behaviour (W2-T05B)
   Vanilla JS. Progressive enhancement over pages/create-team.jsp.

   This script never changes the form contract: the posted fields (teamName,
   repeated playerIds checkboxes, submit=create-team) are exactly what the
   JSP renders. Everything here is a preview — live budget totals, search,
   selected-player chips. The budget math is UX only; the server remains the
   source of truth. All lookups are guarded so the script is inert when the
   form is not on the page (e.g. logged-out state).
   ========================================================================== */
(function () {
  "use strict";

  var form = document.getElementById("createTeamForm");
  if (!form) return; // logged-out gate or different page — do nothing

  var budget = parseFloat(form.getAttribute("data-budget")) || 0;

  var picks = Array.prototype.slice.call(form.querySelectorAll('input[name="playerIds"]'));
  // The pool is a CSS grid rather than a <table>, so rows are marked explicitly.
  var rows = Array.prototype.slice.call(form.querySelectorAll("[data-team-row]"));

  var searchInput = document.getElementById("playerSearch");
  var budgetUsed = document.getElementById("budgetUsed");
  var budgetRemaining = document.getElementById("budgetRemaining");
  var selectedCount = document.getElementById("selectedCount");
  var selectedList = document.getElementById("selectedList");
  var overWarning = document.getElementById("overBudgetWarning");

  // Mirrors the money tag exactly — "R12,5m": no space after R, en-ZA comma
  // decimal, always one decimal place, and the millions suffix. Values and the
  // budget are both on the millions scale, so the live totals have to read the
  // same way as the server-rendered ones beside them.
  function formatValue(n) {
    return "R" + n.toLocaleString("en-ZA", { minimumFractionDigits: 1, maximumFractionDigits: 1 }) + "m";
  }

  /* Budget & selection preview (UX only — backend calculates finals) */
  function refreshPreview() {
    var total = 0;
    var count = 0;

    if (selectedList) selectedList.innerHTML = "";

    picks.forEach(function (pick) {
      // Tint the whole row of a picked player, so the choice stays visible
      // while scrolling a long pool.
      var row = pick.closest("[data-team-row]");
      if (row) row.classList.toggle("is-picked", pick.checked);

      if (!pick.checked) return;

      count++;
      total += parseFloat(pick.getAttribute("data-value")) || 0;

      if (selectedList) {
        var name = pick.getAttribute("data-player-name") || "Player";
        var li = document.createElement("li");
        li.appendChild(document.createTextNode(name));

        var remove = document.createElement("button");
        remove.type = "button"; // never submits
        remove.textContent = "×";
        remove.setAttribute("aria-label", "Remove " + name);
        remove.addEventListener("click", function () {
          pick.checked = false;
          refreshPreview();
        });

        li.appendChild(remove);
        selectedList.appendChild(li);
      }
    });

    var remaining = budget - total;

    if (budgetUsed) budgetUsed.textContent = formatValue(total);
    if (budgetRemaining) {
      budgetRemaining.textContent = formatValue(remaining);
      budgetRemaining.classList.toggle("over", remaining < 0);
    }
    if (selectedCount) selectedCount.textContent = String(count);
    if (overWarning) overWarning.hidden = remaining >= 0;
  }

  picks.forEach(function (pick) {
    pick.addEventListener("change", refreshPreview);
  });

  /* Search: hide rows whose text does not match. Hidden rows keep their
     checked checkboxes, so the selection still submits. */
  if (searchInput) {
    searchInput.addEventListener("input", function () {
      var term = searchInput.value.trim().toLowerCase();
      rows.forEach(function (row) {
        row.hidden = term !== "" && row.textContent.toLowerCase().indexOf(term) === -1;
      });
    });
  }

  /* Initial paint (covers selections re-rendered by the JSP) */
  refreshPreview();
})();
