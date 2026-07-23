/* ============================================================================
   Leagues page — "Your leagues" / "Discover" tab switch.
   Both panels are rendered server-side; switching just toggles which is shown,
   so there is no reload and no refetch.
   ========================================================================== */
document.addEventListener("DOMContentLoaded", function () {
    var tabs = Array.prototype.slice.call(document.querySelectorAll("[data-lg-tab]"));
    if (!tabs.length) return;

    function select(name) {
        tabs.forEach(function (tab) {
            var active = tab.getAttribute("data-lg-tab") === name;
            tab.classList.toggle("is-active", active);
            tab.setAttribute("aria-selected", active ? "true" : "false");
        });
        document.querySelectorAll("[data-lg-panel]").forEach(function (panel) {
            panel.hidden = panel.getAttribute("data-lg-panel") !== name;
        });
    }

    tabs.forEach(function (tab) {
        tab.addEventListener("click", function () {
            select(tab.getAttribute("data-lg-tab"));
        });
    });

    var initial = tabs.filter(function (t) { return t.classList.contains("is-active"); })[0] || tabs[0];
    select(initial.getAttribute("data-lg-tab"));
});
