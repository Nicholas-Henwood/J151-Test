document.addEventListener("DOMContentLoaded", function () {
    var recommendationsSection = document.getElementById("transferRecommendations");
    if (!recommendationsSection) {
        return;
    }

    var cards = recommendationsSection.querySelectorAll(".recommendation-card");

    cards.forEach(function (card) {
        var toggleButton = card.querySelector(".recommendation-card__toggle");
        var reason = card.querySelector(".recommendation-card__reason");

        if (toggleButton && reason) {
            toggleButton.addEventListener("click", function () {
                var isHidden = reason.hasAttribute("hidden");

                if (isHidden) {
                    reason.removeAttribute("hidden");
                } else {
                    reason.setAttribute("hidden", "");
                }

                toggleButton.setAttribute("aria-expanded", isHidden ? "true" : "false");
            });
        }

        var applyButton = card.querySelector(".recommendation-card__apply");

        if (applyButton) {
            applyButton.addEventListener("click", function () {
                var playerId = card.getAttribute("data-player-id");
                if (!playerId) {
                    return;
                }

                var targetInput = document.querySelector(
                    'input[name="addedPlayerId"][value="' + playerId + '"]'
                );

                if (targetInput) {
                    targetInput.checked = true;
                    targetInput.scrollIntoView({ behavior: "smooth", block: "center" });
                }
            });
        }
    });
});