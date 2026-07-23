/* ============================================================================
   League members — copy the invite code to the clipboard.
   Inert when there is no code (public leagues have none).
   ========================================================================== */
document.addEventListener("DOMContentLoaded", function () {
    var button = document.getElementById("copyCode");
    if (!button) return;

    var code = button.getAttribute("data-code") || "";

    button.addEventListener("click", function () {
        var done = function () {
            button.classList.add("is-copied");
            button.setAttribute("aria-label", "Copied");
            setTimeout(function () {
                button.classList.remove("is-copied");
                button.setAttribute("aria-label", "Copy invite code");
            }, 1500);
        };

        if (navigator.clipboard && navigator.clipboard.writeText) {
            navigator.clipboard.writeText(code).then(done).catch(fallback);
        } else {
            fallback();
        }

        function fallback() {
            // Older browsers / insecure contexts: select a temporary field and copy.
            var field = document.createElement("textarea");
            field.value = code;
            field.style.position = "fixed";
            field.style.opacity = "0";
            document.body.appendChild(field);
            field.select();
            try { document.execCommand("copy"); done(); } catch (e) { /* no-op */ }
            document.body.removeChild(field);
        }
    });
});
