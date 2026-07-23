document.addEventListener("DOMContentLoaded", function () {
    document.addEventListener("click", function (event) {
        var link = event.target.closest("a[href]");
        if (!link) {
            return;
        }
        if (link.target === "_blank" || link.hasAttribute("download")) {
            return;
        }
        if (event.defaultPrevented || event.metaKey || event.ctrlKey || event.shiftKey || event.altKey || event.button !== 0) {
            return;
        }

        var url;
        try {
            url = new URL(link.href, window.location.href);
        } catch (e) {
            return;
        }

        if (url.origin !== window.location.origin) {
            return;
        }
        if (url.pathname === window.location.pathname && url.search === window.location.search && url.hash) {
            return;
        }

        event.preventDefault();
        document.body.classList.add("page-fade-out");
        setTimeout(function () {
            window.location.href = link.href;
        }, 150);
    });
});
