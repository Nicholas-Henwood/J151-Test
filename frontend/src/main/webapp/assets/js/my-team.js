/* ============================================================================
   My Team page — build the pitch, bench and detail panel from the rendered
   player rows. Everything is client-side; the servlet just supplies the data.

   The JSP emits one hidden <div class="mt-player" data-*> per selection. This
   splits them into starters (squadRole STARTING) and bench (BENCH), lays the
   starters out in a standard rugby XV, renders the bench strip, and wires the
   click-to-inspect detail panel. Defaults the panel to the captain.
   ========================================================================== */
(function () {
    "use strict";

    var root = document.getElementById("myTeam");
    if (!root) return;

    var rows = Array.prototype.slice.call(root.querySelectorAll(".mt-player"));
    if (!rows.length) return;

    /* Position group → pitch slots, keyed by normalised position name. The DB
       positions are groups (2 props, 2 locks, 3 loose forwards, ...), so each
       group's starters fill its slots in selection order. x/y are percentages;
       jerseys carry the standard 1–15 numbering. */
    var FORMATION = {
        "prop":          [{ n: 1, x: 35, y: 11 }, { n: 3, x: 65, y: 11 }],
        "hooker":        [{ n: 2, x: 50, y: 11 }],
        "lock":          [{ n: 4, x: 42, y: 23 }, { n: 5, x: 58, y: 23 }],
        "loose forward": [{ n: 6, x: 30, y: 35 }, { n: 7, x: 70, y: 35 }, { n: 8, x: 50, y: 36 }],
        "flanker":       [{ n: 6, x: 30, y: 35 }, { n: 7, x: 70, y: 35 }],
        "no.8":          [{ n: 8, x: 50, y: 36 }],
        "number 8":      [{ n: 8, x: 50, y: 36 }],
        "eighthman":     [{ n: 8, x: 50, y: 36 }],
        "scrum half":    [{ n: 9, x: 40, y: 48 }],
        "scrum-half":    [{ n: 9, x: 40, y: 48 }],
        "scrumhalf":     [{ n: 9, x: 40, y: 48 }],
        "fly half":      [{ n: 10, x: 57, y: 52 }],
        "fly-half":      [{ n: 10, x: 57, y: 52 }],
        "flyhalf":       [{ n: 10, x: 57, y: 52 }],
        "centre":        [{ n: 12, x: 38, y: 67 }, { n: 13, x: 60, y: 67 }],
        "center":        [{ n: 12, x: 38, y: 67 }, { n: 13, x: 60, y: 67 }],
        "wing":          [{ n: 11, x: 14, y: 71 }, { n: 14, x: 86, y: 71 }],
        "fullback":      [{ n: 15, x: 50, y: 88 }]
    };

    var FORWARDS = ["prop", "hooker", "lock", "loose forward", "flanker", "no.8", "number 8", "eighthman"];

    function norm(name) {
        return (name || "").trim().toLowerCase();
    }

    function isForward(position) {
        return FORWARDS.indexOf(norm(position)) !== -1;
    }

    function shortName(name) {
        var parts = (name || "").trim().split(/\s+/).filter(Boolean);
        if (parts.length < 2) return name || "";
        return parts[0].charAt(0) + ". " + parts[parts.length - 1];
    }

    function initials(name) {
        var parts = (name || "").trim().split(/\s+/).filter(Boolean);
        if (!parts.length) return "";
        return (parts[0].charAt(0) + (parts.length > 1 ? parts[parts.length - 1].charAt(0) : "")).toUpperCase();
    }

    function data(row) {
        return {
            id: row.getAttribute("data-id"),
            name: row.getAttribute("data-name"),
            position: row.getAttribute("data-position"),
            club: row.getAttribute("data-club"),
            value: row.getAttribute("data-value"),
            role: row.getAttribute("data-role"),
            captain: row.getAttribute("data-captain") === "true",
            vice: row.getAttribute("data-vice") === "true",
            active: row.getAttribute("data-active") === "true",
            points: row.getAttribute("data-points"),
            ratings: {
                Attacking: +row.getAttribute("data-attacking") || 0,
                Defensive: +row.getAttribute("data-defensive") || 0,
                Kicking: +row.getAttribute("data-kicking") || 0,
                Discipline: +row.getAttribute("data-discipline") || 0,
                Consistency: +row.getAttribute("data-consistency") || 0,
                Fitness: +row.getAttribute("data-fitness") || 0,
                Form: +row.getAttribute("data-form") || 0
            }
        };
    }

    var players = rows.map(data);
    var starters = players.filter(function (p) { return norm(p.role) !== "bench"; });
    var bench = players.filter(function (p) { return norm(p.role) === "bench"; });

    /* ---------- Pitch ---------- */
    var pitch = document.getElementById("mtPitch");
    var usedSlots = {};      // per-group index of the next free slot
    var fallbackY = 60;      // where unmappable/overflow starters stack

    starters.forEach(function (player) {
        var group = FORMATION[norm(player.position)];
        var slot;
        if (group) {
            var i = usedSlots[player.position] || 0;
            slot = group[i];
            usedSlots[player.position] = i + 1;
        }
        // Unknown position, or more starters in a group than slots: stack along a
        // fallback row rather than dropping the player.
        if (!slot) {
            slot = { n: "", x: 8 + (fallbackY % 90), y: 96 };
            fallbackY += 22;
        }

        var node = document.createElement("div");
        node.className = "mt-node";
        node.style.left = slot.x + "%";
        node.style.top = slot.y + "%";
        node.setAttribute("data-player-id", player.id);

        var jersey = '<span class="mt-jersey ' + (isForward(player.position) ? "mt-jersey-fwd" : "mt-jersey-back") + '">'
            + slot.n
            + (player.captain ? '<span class="mt-badge mt-badge-c">C</span>' : (player.vice ? '<span class="mt-badge mt-badge-v">V</span>' : ''))
            + '</span>';

        node.innerHTML = jersey
            + '<span class="mt-node-name">' + escapeHtml(shortName(player.name)) + '</span>'
            + '<span class="mt-node-pts">' + escapeHtml(player.points || "0") + ' pts</span>';

        node.addEventListener("click", function () { select(player, node); });
        pitch.appendChild(node);
        player._node = node;
    });

    /* ---------- Bench ---------- */
    var benchWrap = document.getElementById("mtBench");
    var benchCount = document.getElementById("mtBenchCount");
    if (benchCount) benchCount.textContent = bench.length + (bench.length === 1 ? " substitute" : " substitutes");

    bench.forEach(function (player) {
        var card = document.createElement("div");
        card.className = "mt-bench-card";
        card.innerHTML =
            '<span class="mt-avatar">' + escapeHtml(initials(player.name)) + '</span>'
            + '<span style="min-width:0">'
            + '<span class="mt-bench-name">' + escapeHtml(player.name) + '</span>'
            + '<span class="mt-bench-sub">'
            + '<span class="mt-pos ' + (isForward(player.position) ? "mt-pos-fwd" : "mt-pos-back") + '">' + escapeHtml(player.position) + '</span>'
            + '<span class="mt-bench-club">' + escapeHtml(player.club) + '</span>'
            + '<span class="mt-bench-pts">' + escapeHtml(player.points || "0") + ' pts</span>'
            + '</span></span>';
        card.addEventListener("click", function () { select(player, card); });
        benchWrap.appendChild(card);
        player._node = card;
    });

    /* ---------- Detail panel ---------- */
    var detail = document.getElementById("mtDetail");
    var selectedNode = null;

    var RATING_ORDER = ["Attacking", "Defensive", "Kicking", "Discipline", "Consistency", "Fitness", "Form"];

    function select(player, node) {
        if (selectedNode) selectedNode.classList.remove("is-selected");
        node.classList.add("is-selected");
        selectedNode = node;

        var role = norm(player.role) === "bench" ? "Substitute" : "Starting XV";
        var capTag = player.captain ? '<span class="mt-cap">Captain</span>'
            : (player.vice ? '<span class="mt-cap">Vice-captain</span>' : '');

        var bars = RATING_ORDER.map(function (label) {
            // currentForm is 0-100 stored; shown on the same 0-100 bar as the abilities.
            var v = player.ratings[label] || 0;
            return '<div class="mt-rating">'
                + '<span class="mt-rating-name">' + label + '</span>'
                + '<span class="mt-rating-track"><span class="mt-rating-fill" style="width:' + Math.max(0, Math.min(100, v)) + '%"></span></span>'
                + '<span class="mt-rating-value">' + v + '</span>'
                + '</div>';
        }).join("");

        detail.innerHTML =
            '<div class="mt-detail-head">'
            + '<span class="mt-avatar">' + escapeHtml(initials(player.name)) + '</span>'
            + '<span style="min-width:0"><p class="mt-detail-name">' + escapeHtml(player.name) + '</p>'
            + '<p class="mt-detail-club">' + escapeHtml(player.club) + '</p></span>'
            + '</div>'
            + '<div class="mt-detail-tags">'
            + '<span class="mt-pos ' + (isForward(player.position) ? "mt-pos-fwd" : "mt-pos-back") + '">' + escapeHtml(player.position) + '</span>'
            + '<span class="mt-role">' + role + '</span>' + capTag
            + '</div>'
            + '<div class="mt-detail-chips">'
            + '<div class="mt-chip"><span class="mt-chip-label">Value</span><span class="mt-chip-value">' + escapeHtml(player.value) + '</span></div>'
            + '<div class="mt-chip"><span class="mt-chip-label">Fantasy</span><span class="mt-chip-value is-gold">' + escapeHtml(player.points || "0") + '</span></div>'
            + '<div class="mt-chip"><span class="mt-chip-label">Active</span><span class="mt-chip-value">' + (player.active ? "Yes" : "No") + '</span></div>'
            + '</div>'
            + '<p class="mt-ratings-label">Player ratings</p>'
            + bars;
    }

    function escapeHtml(s) {
        return String(s == null ? "" : s)
            .replace(/&/g, "&amp;").replace(/</g, "&lt;").replace(/>/g, "&gt;")
            .replace(/"/g, "&quot;").replace(/'/g, "&#39;");
    }

    // Default to the captain, else the first starter, else the first bench player.
    var initial = players.filter(function (p) { return p.captain; })[0]
        || starters[0] || bench[0];
    if (initial && initial._node) select(initial, initial._node);
})();
