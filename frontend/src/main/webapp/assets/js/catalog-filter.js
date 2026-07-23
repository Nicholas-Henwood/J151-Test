/* ============================================================================
   Catalog pages — live filter + sort (players, clubs, ...).

   The servlet renders the whole list; search, the filter dropdowns and the sort
   control all recompute the visible rows here, so nothing round-trips.

   Wiring is declarative, so a new catalog page needs no JS changes:

     [data-catalog]            container, data-catalog-noun="player" names the
                               count label ("12 players · season 2025/26")
     [data-catalog-body]       holds the .crow rows
     [data-catalog-table]      hidden when nothing matches
     [data-catalog-empty]      shown when nothing matches
     [data-catalog-count]      receives the filtered count
     [data-catalog-search]     substring match against each row's data-name
     [data-catalog-filter=key] exact match of the control's value against
                               row.dataset[key]; empty value means "all"
     [data-catalog-sort]       its options carry value="<row data key>" plus
                               data-dir="asc|desc" and data-type="text|number"
   ========================================================================== */
document.addEventListener("DOMContentLoaded", function () {
    var root = document.querySelector("[data-catalog]");
    if (!root) return;

    var body = root.querySelector("[data-catalog-body]");
    if (!body) return;

    var table = root.querySelector("[data-catalog-table]");
    var empty = root.querySelector("[data-catalog-empty]");
    var countLabel = root.querySelector("[data-catalog-count]");
    var search = root.querySelector("[data-catalog-search]");
    var sortSelect = root.querySelector("[data-catalog-sort]");
    var filters = Array.prototype.slice.call(root.querySelectorAll("[data-catalog-filter]"));

    var noun = root.getAttribute("data-catalog-noun") || "result";
    var suffix = root.getAttribute("data-catalog-count-suffix") || "";
    // Any direct child of the body is a row, so this drives both the table pages
    // (.crow divs) and the card grids (.lg-card articles) without either having
    // to adopt the other's class.
    var rows = Array.prototype.slice.call(body.children);

    function matches(row) {
        if (search) {
            var term = search.value.trim().toLowerCase();
            if (term && (row.dataset.name || "").indexOf(term) === -1) return false;
        }
        for (var i = 0; i < filters.length; i++) {
            var control = filters[i];
            var key = control.getAttribute("data-catalog-filter");
            if (control.value && row.dataset[key] !== control.value) return false;
        }
        return true;
    }

    function compare(a, b) {
        if (!sortSelect) return 0;
        var option = sortSelect.options[sortSelect.selectedIndex];
        if (!option) return 0;

        var key = option.value;
        var descending = option.getAttribute("data-dir") === "desc";
        var numeric = option.getAttribute("data-type") === "number";

        var first = a.dataset[key] || "";
        var second = b.dataset[key] || "";
        var result = numeric
            ? (parseFloat(first) || 0) - (parseFloat(second) || 0)
            : first.localeCompare(second);
        return descending ? -result : result;
    }

    function apply() {
        var visible = rows.filter(matches);
        visible.sort(compare);

        rows.forEach(function (row) { row.hidden = true; });
        visible.forEach(function (row) {
            row.hidden = false;
            body.appendChild(row);
        });

        if (table) table.hidden = visible.length === 0;
        if (empty) empty.hidden = visible.length !== 0;
        if (countLabel) {
            countLabel.textContent = visible.length + " " + noun + (visible.length === 1 ? "" : "s") + suffix;
        }
    }

    if (search) search.addEventListener("input", apply);
    filters.concat(sortSelect ? [sortSelect] : []).forEach(function (control) {
        control.addEventListener("change", apply);
    });

    apply();
});
