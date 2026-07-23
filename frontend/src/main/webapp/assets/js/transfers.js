/* ============================================================================
   TryTons — Transfers page behaviour.

   Selection, value/budget maths, penalty gating and confirm-enablement are all
   client-side; the final submit posts to /transfers exactly as before. Every
   lookup is guarded so the script is inert when the form is absent.

   Money is rendered to match the money tag: "R12,5m" — en-ZA comma decimal,
   one decimal place, millions suffix, no space after the R.
   ========================================================================== */
(function () {
  "use strict";

  var form = document.getElementById("transferForm");
  if (!form) return;

  var outInput = document.getElementById("removedPlayerId");
  var inInput = document.getElementById("addedPlayerId");
  var confirmBtn = document.getElementById("confirmTransfer");
  var clearBtn = document.getElementById("clearSelection");
  var penaltyWrap = document.getElementById("penaltyWrap");
  var penaltyCheck = document.getElementById("penaltyConfirmed");
  var affordWarning = document.getElementById("affordWarning");
  var valueChange = document.getElementById("valueChange");
  var budgetAfter = document.getElementById("budgetAfter");
  var searchInput = document.getElementById("availableSearch");
  var availableEmpty = document.getElementById("availableEmpty");

  var budget = parseFloat(form.getAttribute("data-budget")) || 0;
  var freeLeft = parseInt(form.getAttribute("data-free-transfers"), 10);
  if (isNaN(freeLeft)) freeLeft = 1;
  var locked = form.getAttribute("data-locked") === "true";

  var outRows = Array.prototype.slice.call(document.querySelectorAll('[data-pick="out"]'));
  var inRows = Array.prototype.slice.call(document.querySelectorAll('[data-pick="in"]'));

  var selected = { out: null, in: null };

  // Sign goes outside the currency symbol — "−R16,0m", never "R-16,0m".
  function money(n) {
    var sign = n < 0 ? "−" : "";
    return sign + "R"
        + Math.abs(n).toLocaleString("en-ZA", { minimumFractionDigits: 1, maximumFractionDigits: 1 })
        + "m";
  }

  function signedMoney(n) {
    if (n === 0) return money(0);
    return (n > 0 ? "+" : "") + money(n);
  }

  function val(row) {
    return parseFloat(row.getAttribute("data-value")) || 0;
  }

  function fillSlot(side, row) {
    var slot = document.getElementById(side === "out" ? "slotOut" : "slotIn");
    if (!slot) return;
    var disc = slot.querySelector(".tf-slot-disc");
    var tag = slot.querySelector(".tf-slot-tag");
    var name = slot.querySelector(".tf-slot-name");
    var meta = slot.querySelector(".tf-slot-meta");

    if (!row) {
      slot.classList.remove("is-filled");
      disc.textContent = side === "out" ? "–" : "+";
      tag.textContent = side === "out" ? "Out" : "In";
      name.textContent = side === "out" ? "Select from your squad" : "Select an available player";
      meta.textContent = "";
      return;
    }

    slot.classList.add("is-filled");
    disc.textContent = row.getAttribute("data-initials") || "";
    tag.textContent = side === "out" ? "Removing" : "Bringing in";
    name.textContent = row.getAttribute("data-name") || "";
    meta.textContent = (row.getAttribute("data-club") || "") + " · " + money(val(row));
  }

  function refresh() {
    var outRow = selected.out;
    var inRow = selected.in;

    fillSlot("out", outRow);
    fillSlot("in", inRow);

    outInput.value = outRow ? outRow.getAttribute("data-player-id") : "";
    inInput.value = inRow ? inRow.getAttribute("data-player-id") : "";

    // Value change is what the swap costs: incoming minus outgoing.
    var diff = (inRow ? val(inRow) : 0) - (outRow ? val(outRow) : 0);
    var after = budget - diff;
    var complete = !!(outRow && inRow);

    if (valueChange) {
      valueChange.textContent = complete ? signedMoney(diff) : "—";
      valueChange.classList.toggle("is-up", complete && diff > 0);
      valueChange.classList.toggle("is-down", complete && diff < 0);
    }
    if (budgetAfter) {
      budgetAfter.textContent = complete ? money(after) : money(budget);
      budgetAfter.classList.toggle("is-negative", complete && after < 0);
      budgetAfter.classList.toggle("is-gold", !(complete && after < 0));
    }

    var affordable = !complete || after >= 0;
    if (affordWarning) affordWarning.hidden = affordable;

    // The backend allows one free transfer per round and charges -4 points
    // beyond that, refusing the transfer unless it is acknowledged.
    var needsPenalty = complete && freeLeft <= 0;
    if (penaltyWrap) penaltyWrap.hidden = !needsPenalty;
    if (!needsPenalty && penaltyCheck) penaltyCheck.checked = false;

    if (clearBtn) clearBtn.hidden = !(outRow || inRow);
    if (confirmBtn) {
      confirmBtn.disabled = locked
          || !complete
          || !affordable
          || (needsPenalty && penaltyCheck && !penaltyCheck.checked);
    }
  }

  function choose(side, row) {
    if (locked) return;
    var rows = side === "out" ? outRows : inRows;
    var already = selected[side] === row;
    selected[side] = already ? null : row;
    rows.forEach(function (r) {
      r.classList.toggle(side === "out" ? "is-out" : "is-in", r === selected[side]);
    });
    refresh();
  }

  outRows.forEach(function (row) {
    row.addEventListener("click", function () { choose("out", row); });
  });
  inRows.forEach(function (row) {
    row.addEventListener("click", function () { choose("in", row); });
  });

  if (penaltyCheck) penaltyCheck.addEventListener("change", refresh);

  if (clearBtn) {
    clearBtn.addEventListener("click", function () {
      selected.out = null;
      selected.in = null;
      outRows.concat(inRows).forEach(function (r) { r.classList.remove("is-out", "is-in"); });
      refresh();
    });
  }

  /* Search the available panel by player name or club. */
  if (searchInput) {
    searchInput.addEventListener("input", function () {
      var term = searchInput.value.trim().toLowerCase();
      var visible = 0;
      inRows.forEach(function (row) {
        var haystack = (row.getAttribute("data-search") || "");
        var match = term === "" || haystack.indexOf(term) !== -1;
        row.hidden = !match;
        if (match) visible++;
      });
      if (availableEmpty) availableEmpty.hidden = visible !== 0;
    });
  }

  /* Recommendations: "Why this pick?" toggle and "Apply to transfer". */
  document.querySelectorAll("[data-rec-why]").forEach(function (toggle) {
    toggle.addEventListener("click", function () {
      var reason = document.getElementById(toggle.getAttribute("data-rec-why"));
      if (!reason) return;
      reason.hidden = !reason.hidden;
      toggle.textContent = reason.hidden ? "Why this pick?" : "Hide reason";
    });
  });

  document.querySelectorAll("[data-rec-apply]").forEach(function (button) {
    button.addEventListener("click", function () {
      var addId = button.getAttribute("data-rec-apply");
      var replacesId = button.getAttribute("data-rec-replaces");

      var inRow = inRows.filter(function (r) { return r.getAttribute("data-player-id") === addId; })[0];
      var outRow = outRows.filter(function (r) { return r.getAttribute("data-player-id") === replacesId; })[0];

      if (inRow) { selected.in = inRow; inRows.forEach(function (r) { r.classList.toggle("is-in", r === inRow); }); }
      if (outRow) { selected.out = outRow; outRows.forEach(function (r) { r.classList.toggle("is-out", r === outRow); }); }
      refresh();

      var summary = document.querySelector(".tf-summary");
      if (summary) summary.scrollIntoView({ behavior: "smooth", block: "center" });
    });
  });

  refresh();
})();
