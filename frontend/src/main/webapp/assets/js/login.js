document.addEventListener("DOMContentLoaded", function () {
    var toggle = document.getElementById("passwordToggle");
    var passwordInput = document.getElementById("password");

    if (toggle && passwordInput) {
        toggle.addEventListener("click", function () {
            var showing = passwordInput.type === "text";
            passwordInput.type = showing ? "password" : "text";
            toggle.setAttribute("aria-label", showing ? "Show password" : "Hide password");
            toggle.classList.toggle("is-visible", !showing);
        });
    }

    var form = document.getElementById("loginForm");
    var submitButton = document.getElementById("loginSubmit");
    var submitLabel = document.getElementById("loginSubmitLabel");

    if (form && submitButton) {
        form.addEventListener("submit", function () {
            if (form.checkValidity()) {
                submitButton.classList.add("is-loading");
                if (submitLabel) {
                    submitLabel.textContent = "Signing in…";
                }
                // Deferred: disabling the submit button synchronously here would
                // strip its name/value from the form data the browser is about
                // to send, since disabled-state is re-checked when the entry
                // list is constructed right after this handler returns.
                setTimeout(function () {
                    submitButton.disabled = true;
                }, 0);
            }
        });
    }
});
