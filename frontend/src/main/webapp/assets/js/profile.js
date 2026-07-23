/* ============================================================================
   Profile page — live hero preview.
   Typing in the username or profile-picture fields updates the hero avatar and
   name for immediate feedback. Nothing here submits; Save posts the form as
   normal. Inert when the form is absent (profile failed to load).
   ========================================================================== */
document.addEventListener("DOMContentLoaded", function () {
    var form = document.getElementById("profileForm");
    if (!form) return;

    var usernameInput = document.getElementById("username");
    var picInput = document.getElementById("profilePic");
    var heroName = document.getElementById("heroUsername");
    var heroImage = document.getElementById("heroAvatarImage");
    var heroInitials = document.getElementById("heroAvatarInitials");

    function initials(name) {
        var parts = name.trim().split(/\s+/).filter(Boolean);
        if (!parts.length) return "";
        var first = parts[0].charAt(0);
        var last = parts.length > 1 ? parts[parts.length - 1].charAt(0) : "";
        return (first + last).toUpperCase();
    }

    if (usernameInput && heroName) {
        usernameInput.addEventListener("input", function () {
            var value = usernameInput.value.trim();
            heroName.textContent = value || "—";
            if (heroInitials) heroInitials.textContent = initials(value);
        });
    }

    if (picInput && heroImage) {
        picInput.addEventListener("input", function () {
            var url = picInput.value.trim();
            if (url) {
                heroImage.src = url;
                heroImage.hidden = false;
            } else {
                // Empty URL falls back to the initials underneath.
                heroImage.removeAttribute("src");
                heroImage.hidden = true;
            }
        });

        // A broken or unreachable URL should not leave a broken-image icon over
        // the initials — hide the img and let the initials show through.
        heroImage.addEventListener("error", function () {
            heroImage.hidden = true;
        });
    }
});
