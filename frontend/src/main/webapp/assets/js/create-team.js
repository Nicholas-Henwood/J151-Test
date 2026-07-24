/* ============================================================================
   TryTons — Fantasy Rugby | Create Team behaviour (W2-T05B)
   Vanilla JS. Progressive enhancement over pages/create-team.jsp.

   This script never changes the form contract: the posted fields (teamName,
   repeated playerIds checkboxes, submit=create-team) are exactly what the
   JSP renders. Everything here is a preview — live budget totals, search,
   selected-player chips, and a per-position requirements helper that blocks
   obviously-illegal picks before they are made. The rules are read from the
   backend position catalogue rendered into the page (.ct-req data-min/-max),
   so they can never drift from what the server validates on submit. The
   server remains the source of truth. All lookups are guarded so the script
   is inert when the form is not on the page (e.g. logged-out state).
   ========================================================================== */
(function () {
  "use strict";

  var form = document.getElementById("createTeamForm");
  if (!form) return; // logged-out gate or different page — do nothing

  var budget = parseFloat(form.getAttribute("data-budget")) || 0;
  var squadSize = parseInt(form.getAttribute("data-squad-size"), 10) || 20;

  var picks = Array.prototype.slice.call(form.querySelectorAll('input[name="playerIds"]'));
  // The pool is a CSS grid rather than a <table>, so rows are marked explicitly.
  var rows = Array.prototype.slice.call(form.querySelectorAll("[data-team-row]"));

  var searchInput = document.getElementById("playerSearch");
  var budgetUsed = document.getElementById("budgetUsed");
  var budgetRemaining = document.getElementById("budgetRemaining");
  var selectedCount = document.getElementById("selectedCount");
  var selectedList = document.getElementById("selectedList");
  var overWarning = document.getElementById("overBudgetWarning");
  var reqSummary = document.getElementById("ctReqSummary");
  var flashEl = document.getElementById("ctFlash");

  // One bucket per position, built from the requirement rows the JSP rendered
  // from the backend catalogue. Keyed by the exact position name that also
  // appears on each player checkbox's data-position, so counting is a lookup.
  var buckets = {};
  Array.prototype.slice.call(form.querySelectorAll("[data-req]")).forEach(function (li) {
    var name = li.getAttribute("data-position");
    if (!name) return;
    buckets[name] = {
      el: li,
      name: name,
      min: parseInt(li.getAttribute("data-min"), 10) || 0,
      max: parseInt(li.getAttribute("data-max"), 10) || Infinity,
      countEl: li.querySelector(".ct-req-count")
    };
  });

  // Mirrors the money tag exactly — "R12,5m": no space after R, en-ZA comma
  // decimal, always one decimal place, and the millions suffix. Values and the
  // budget are both on the millions scale, so the live totals have to read the
  // same way as the server-rendered ones beside them.
  function formatValue(n) {
    return "R" + n.toLocaleString("en-ZA", { minimumFractionDigits: 1, maximumFractionDigits: 1 }) + "m";
  }

  function checkedPicks() {
    return picks.filter(function (p) { return p.checked; });
  }

  function countInBucket(name) {
    var n = 0;
    picks.forEach(function (p) {
      if (p.checked && p.getAttribute("data-position") === name) n++;
    });
    return n;
  }

  var flashTimer;
  function flash(message) {
    if (!flashEl) { return; }
    flashEl.textContent = message;
    flashEl.hidden = false;
    // Force reflow so the transition replays even on back-to-back messages.
    void flashEl.offsetWidth;
    flashEl.classList.add("is-visible");
    clearTimeout(flashTimer);
    flashTimer = setTimeout(function () {
      flashEl.classList.remove("is-visible");
      flashTimer = setTimeout(function () { flashEl.hidden = true; }, 250);
    }, 3400);
  }

  // Returns a message if checking this pick would break a hard squad rule,
  // otherwise null. Hard rules are the ones a user cannot un-break by adding
  // more players: the 20-man cap and each position's maximum.
  function violationFor(justChecked) {
    if (checkedPicks().length > squadSize) {
      return "Your squad is full — you can only pick " + squadSize +
        " players. Remove one before adding another.";
    }
    var name = justChecked.getAttribute("data-position");
    var bucket = buckets[name];
    if (bucket && countInBucket(name) > bucket.max) {
      return "You can only pick up to " + bucket.max + " " + name +
        " player" + (bucket.max === 1 ? "" : "s") + " — you already have " + bucket.max + ".";
    }
    return null;
  }

  // Update the per-position rows and the one-line summary of what is still off.
  function refreshRequirements() {
    var shortfalls = [];
    var overs = [];

    Object.keys(buckets).forEach(function (name) {
      var b = buckets[name];
      var count = countInBucket(name);
      if (b.countEl) b.countEl.textContent = String(count);

      var state = "is-under";
      if (count > b.max) state = "is-over";
      else if (count >= b.min) state = "is-ok";
      b.el.classList.remove("is-under", "is-ok", "is-over");
      b.el.classList.add(state);

      if (count < b.min) shortfalls.push((b.min - count) + " " + name);
      if (count > b.max) overs.push(name);
    });

    if (!reqSummary) return;

    var total = checkedPicks().length;
    var parts = [];
    if (total < squadSize) parts.push("Pick " + (squadSize - total) + " more");
    else if (total > squadSize) parts.push("Remove " + (total - squadSize));
    if (shortfalls.length) parts.push("still need " + shortfalls.join(", "));
    if (overs.length) parts.push("too many " + overs.join(", "));

    if (parts.length === 0) {
      reqSummary.textContent = "Squad is complete and legal ✓";
      reqSummary.classList.add("is-complete");
    } else {
      reqSummary.textContent = parts.join(" · ");
      reqSummary.classList.remove("is-complete");
    }
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

    refreshRequirements();
  }

  function onPickChange(e) {
    var pick = e.target;
    if (pick.checked) {
      var problem = violationFor(pick);
      if (problem) {
        pick.checked = false; // revert the illegal pick (does not re-fire change)
        flash(problem);
      }
    }
    refreshPreview();
  }

  picks.forEach(function (pick) {
    pick.addEventListener("change", onPickChange);
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
