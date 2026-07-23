/* ============================================================================
   Fixtures page — status filter pills.

   The servlet already supports server-side filtering via ?status=, but every
   fixture is rendered, so switching filters here needs no reload. Round groups
   hide themselves once all their cards are filtered out, and the shared empty
   state appears when nothing matches at all.
   ========================================================================== */
document.addEventListener("DOMContentLoaded", function () {
    var tabs = Array.prototype.slice.call(document.querySelectorAll("[data-status-tab]"));
    if (!tabs.length) return;

    var cards = Array.prototype.slice.call(document.querySelectorAll("[data-fixture-status]"));
    var groups = Array.prototype.slice.call(document.querySelectorAll("[data-round-group]"));
    var empty = document.getElementById("fixturesEmptyState");

    function apply(status) {
        var visible = 0;

        cards.forEach(function (card) {
            var match = status === "" || card.getAttribute("data-fixture-status") === status;
            card.hidden = !match;
            if (match) visible++;
        });

        // A round header with no remaining fixtures would otherwise sit alone.
        groups.forEach(function (group) {
            var shown = group.querySelectorAll("[data-fixture-status]:not([hidden])").length;
            group.hidden = shown === 0;
        });

        if (empty) empty.hidden = visible !== 0;

        tabs.forEach(function (tab) {
            var active = tab.getAttribute("data-status-tab") === status;
            tab.classList.toggle("is-active", active);
            tab.setAttribute("aria-pressed", active ? "true" : "false");
        });
    }

    tabs.forEach(function (tab) {
        tab.addEventListener("click", function () {
            apply(tab.getAttribute("data-status-tab"));
        });
    });

    // Honour the server-rendered filter so a ?status= link and the pills agree.
    var initial = tabs.filter(function (t) { return t.classList.contains("is-active"); })[0] || tabs[0];
    apply(initial.getAttribute("data-status-tab"));
});
