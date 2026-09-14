(() => {
  const table = document.querySelector(".rating-table");
  if (!table) return;

  const portraitPhone = window.matchMedia("(max-width: 600px) and (orientation: portrait)");

  function fitSummaryColumn() {
    table.style.zoom = "";

    const summary = table.tHead.rows[0].cells[6];
    const left = table.getBoundingClientRect().left;
    const summaryRight = summary.getBoundingClientRect().right;
    const needed = summaryRight - left;
    table.style.setProperty("--summary-width", `${needed}px`);

    if (!portraitPhone.matches) return;

    const available = document.documentElement.clientWidth - left - 8;

    if (needed > available && available > 0) {
      table.style.zoom = String(available / needed);
    }
  }

  window.addEventListener("load", fitSummaryColumn);
  window.addEventListener("resize", fitSummaryColumn);
  document.fonts?.ready.then(fitSummaryColumn);
  fitSummaryColumn();
})();
