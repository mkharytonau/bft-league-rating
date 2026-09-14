(function () {
  function normalize(text) {
    return (text || "").trim().toLowerCase();
  }

  // pairs a summary row with the detail row immediately following it (if
  // any, marked with class "detail-row"), so search/sort treat them as one
  // unit and keep them adjacent
  function rowGroups(tbody) {
    var groups = [];
    var rows = Array.prototype.slice.call(tbody.children);
    for (var i = 0; i < rows.length; i++) {
      var row = rows[i];
      if (row.classList.contains("detail-row")) continue;
      var next = rows[i + 1];
      var detail =
        next && next.classList.contains("detail-row") ? next : null;
      groups.push({ summary: row, detail: detail });
    }
    return groups;
  }

  function setupSearch(toolbar) {
    var input = toolbar.querySelector(".table-search");
    var table = toolbar.nextElementSibling;
    if (!input || !table || !table.tBodies[0]) return;
    var groups = rowGroups(table.tBodies[0]);

    input.addEventListener("input", function () {
      var query = normalize(input.value);
      groups.forEach(function (group) {
        var haystack =
          normalize(group.summary.textContent) +
          " " +
          (group.detail ? normalize(group.detail.textContent) : "");
        var matches = !query || haystack.indexOf(query) !== -1;
        group.summary.style.display = matches ? "" : "none";
        if (group.detail) {
          group.detail.style.display =
            matches && group.summary.classList.contains("expanded")
              ? ""
              : "none";
        }
      });
    });
  }

  function cellValue(row, index) {
    var cell = row.children[index];
    if (!cell) return "";
    return cell.getAttribute("data-sort") || cell.textContent || "";
  }

  function setupSort(table) {
    if (!table.tHead || !table.tBodies[0]) return;
    var headers = Array.prototype.slice.call(table.tHead.rows[0].cells);
    var tbody = table.tBodies[0];

    headers.forEach(function (th, index) {
      // blank headers (e.g. the row-expand chevron column) have nothing
      // sortable in them
      if (!th.textContent.trim()) return;
      th.classList.add("sortable");
      th.addEventListener("click", function () {
        var direction =
          th.getAttribute("aria-sort") === "ascending"
            ? "descending"
            : "ascending";
        headers.forEach(function (h) {
          h.removeAttribute("aria-sort");
        });
        th.setAttribute("aria-sort", direction);

        var groups = rowGroups(tbody);
        var numeric = groups.every(function (g) {
          var value = cellValue(g.summary, index).replace(",", ".").trim();
          return value === "" || value === "-" || !isNaN(parseFloat(value));
        });

        groups.sort(function (a, b) {
          var va = cellValue(a.summary, index);
          var vb = cellValue(b.summary, index);
          var cmp;
          if (numeric) {
            cmp = (parseFloat(va.replace(",", ".")) || 0) -
              (parseFloat(vb.replace(",", ".")) || 0);
          } else {
            cmp = va.localeCompare(vb, "ru");
          }
          return direction === "ascending" ? cmp : -cmp;
        });

        groups.forEach(function (group) {
          tbody.appendChild(group.summary);
          if (group.detail) tbody.appendChild(group.detail);
        });
      });
    });
  }

  // rows can carry a detail row (added by RatingWriter for the per-event
  // breakdown); clicking the summary row toggles it
  function setupExpand(table) {
    if (!table.tBodies[0]) return;
    rowGroups(table.tBodies[0]).forEach(function (group) {
      if (!group.detail) return;
      group.detail.style.display = "none";
      group.summary.classList.add("has-detail");
      group.summary.addEventListener("click", function (event) {
        if (event.target.closest("a")) return;
        var expanded = group.summary.classList.toggle("expanded");
        group.detail.style.display = expanded ? "" : "none";
      });
    });
  }

  document.querySelectorAll(".table-toolbar").forEach(setupSearch);
  document.querySelectorAll("table.enhanced-table").forEach(function (table) {
    setupSort(table);
    setupExpand(table);
  });
})();
