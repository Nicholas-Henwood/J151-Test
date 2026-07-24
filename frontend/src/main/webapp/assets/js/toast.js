/* ============================================================================
   TryTons — Fantasy Rugby | Toast feedback
   Vanilla JS. Drives the system-wide success/error/info toasts rendered by
   /WEB-INF/jspf/toast.jspf. Two entry points:

     1. Server flash — the JSPF renders one <div class="toast" data-toast> from
        a session flash message; on load we animate it in and auto-dismiss it.
     2. Client trigger — window.TrytonsToast.show(message, type) pops one from
        page JS (e.g. after an in-page success), no round-trip needed.

   Styling and motion live in theme.css (.toast-*). This file only manages the
   lifecycle: enter, auto-dismiss, manual close, and stacking. All lookups are
   guarded so the script is inert on pages without the stack.
   ========================================================================== */
(function (window, document) {
  "use strict";

  var STACK_ID = "toastStack";
  var VISIBLE_MS = 3500;   // how long a toast lingers before auto-dismiss
  var LEAVE_MS = 260;      // must clear the CSS transition (.22s) before removal
  var TYPES = { success: 1, error: 1, info: 1 };

  // The stack is normally rendered by the JSPF. If a page calls show() without
  // it (e.g. a page that omits the sidebar), create it on demand so the API is
  // always usable.
  function stack() {
    var el = document.getElementById(STACK_ID);
    if (!el) {
      el = document.createElement("div");
      el.id = STACK_ID;
      el.className = "toast-stack";
      el.setAttribute("aria-live", "polite");
      el.setAttribute("aria-atomic", "false");
      document.body.appendChild(el);
    }
    return el;
  }

  function dismiss(toast) {
    if (toast.dataset.leaving) return; // already on its way out
    toast.dataset.leaving = "1";
    clearTimeout(toast._timer);
    toast.classList.remove("is-visible");
    toast.classList.add("is-leaving");
    setTimeout(function () {
      if (toast.parentNode) toast.parentNode.removeChild(toast);
    }, LEAVE_MS);
  }

  // Wire a toast that is already in the DOM: close button, auto-dismiss, and
  // the enter transition. Used for both server-rendered and show()-created ones.
  function activate(toast) {
    var close = toast.querySelector(".toast-close");
    if (close) {
      close.addEventListener("click", function () { dismiss(toast); });
    }
    // Force reflow so the transition plays from the hidden state even for a
    // node that was in the initial HTML.
    void toast.offsetWidth;
    toast.classList.add("is-visible");
    toast._timer = setTimeout(function () { dismiss(toast); }, VISIBLE_MS);
  }

  function build(message, type) {
    var kind = TYPES[type] ? type : "success";
    var toast = document.createElement("div");
    toast.className = "toast toast-" + kind;
    // Errors interrupt (assertive); success/info wait their turn (polite).
    toast.setAttribute("role", kind === "error" ? "alert" : "status");

    var msg = document.createElement("span");
    msg.className = "toast-msg";
    msg.textContent = message; // textContent — never inject markup from callers
    toast.appendChild(msg);

    var close = document.createElement("button");
    close.type = "button";
    close.className = "toast-close";
    close.setAttribute("aria-label", "Dismiss");
    close.textContent = "×"; // times sign
    toast.appendChild(close);

    return toast;
  }

  // Public: pop a toast from page JS. type is "success" (default), "error", or
  // "info". Returns the toast element in case the caller wants to dismiss early.
  function show(message, type) {
    if (!message) return null;
    var toast = build(String(message), type);
    stack().appendChild(toast);
    activate(toast);
    return toast;
  }

  function init() {
    var el = document.getElementById(STACK_ID);
    if (!el) return;
    // Adopt any toasts the server rendered into the stack.
    Array.prototype.slice.call(el.querySelectorAll("[data-toast]")).forEach(activate);
  }

  if (document.readyState === "loading") {
    document.addEventListener("DOMContentLoaded", init);
  } else {
    init();
  }

  window.TrytonsToast = { show: show, dismiss: dismiss };
})(window, document);
