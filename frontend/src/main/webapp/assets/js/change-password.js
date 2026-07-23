/* ============================================================================
   Change-password page — show/hide toggles and the confirm-match guard.
   Confirmation is client-side only; the server receives currentPassword and
   newPassword. Inert when the form is absent.
   ========================================================================== */
document.addEventListener("DOMContentLoaded", function () {
    var form = document.getElementById("changePasswordForm");
    if (!form) return;

    // Each toggle button carries data-toggle="<input id>".
    document.querySelectorAll("[data-toggle]").forEach(function (button) {
        var input = document.getElementById(button.getAttribute("data-toggle"));
        if (!input) return;
        button.addEventListener("click", function () {
            var showing = input.type === "text";
            input.type = showing ? "password" : "text";
            button.classList.toggle("is-visible", !showing);
            button.setAttribute("aria-label", showing ? "Show password" : "Hide password");
        });
    });

    var newPassword = document.getElementById("newPassword");
    var confirmPassword = document.getElementById("confirmPassword");
    var mismatch = document.getElementById("passwordMismatchWarning");

    form.addEventListener("submit", function (event) {
        if (newPassword.value !== confirmPassword.value) {
            event.preventDefault();
            if (mismatch) mismatch.hidden = false;
        } else if (mismatch) {
            mismatch.hidden = true;
        }
    });
});
