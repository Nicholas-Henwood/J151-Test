/* ============================================================================
   Notifications page — the "show unread only" toggle.
   The filter is a server query (?unreadOnly=true), so flipping the switch just
   submits its form. Kept out of the markup so the page carries no inline JS.
   ========================================================================== */
document.addEventListener("DOMContentLoaded", function () {
    var toggle = document.getElementById("unreadOnlyToggle");
    if (!toggle || !toggle.form) return;

    toggle.addEventListener("change", function () {
        toggle.form.submit();
    });
});
