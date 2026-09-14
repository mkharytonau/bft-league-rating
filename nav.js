(function () {
  // The season folder ("2026", "2025", ...) shows up as a 4-digit path
  // segment somewhere above the current page; everything below it is
  // relative depth we need to climb back out of to reach season-level
  // pages like rating.html/statistics.html.
  var DEFAULT_SEASON = "2026";
  var directories = location.pathname.split("/").filter(Boolean).slice(0, -1);
  var yearIndex = directories.findIndex(function (segment) {
    return /^\d{4}$/.test(segment);
  });

  var seasonPrefix;
  if (yearIndex === -1) {
    // not under any season folder (e.g. the repo-root index.html), fall
    // back to the same path root's own links use for the current season
    seasonPrefix = "src/main/resources/" + DEFAULT_SEASON + "/";
  } else {
    var levelsBelowSeason = directories.length - yearIndex - 1;
    seasonPrefix = "../".repeat(levelsBelowSeason);
  }

  function isCurrent(href) {
    var a = document.createElement("a");
    a.href = href;
    return a.pathname === location.pathname;
  }

  function link(href, label) {
    var a = document.createElement("a");
    a.href = href;
    a.textContent = label;
    if (isCurrent(href)) a.setAttribute("aria-current", "page");
    return a;
  }

  var nav = document.createElement("nav");
  nav.className = "site-nav";

  var toggle = document.createElement("button");
  toggle.type = "button";
  toggle.className = "nav-toggle";
  toggle.setAttribute("aria-expanded", "false");
  toggle.setAttribute("aria-label", "Меню");
  toggle.innerHTML = '<span class="nav-toggle-icon">☰</span> Меню';
  toggle.addEventListener("click", function () {
    var open = nav.classList.toggle("open");
    toggle.setAttribute("aria-expanded", open ? "true" : "false");
  });
  nav.appendChild(toggle);

  var links = document.createElement("div");
  links.className = "nav-links";

  links.appendChild(link(seasonPrefix + "rating.html", "Рейтинг"));
  links.appendChild(link(seasonPrefix + "statistics.html", "Статистика"));

  var dropdown = document.createElement("details");
  dropdown.className = "nav-dropdown";
  var summary = document.createElement("summary");
  summary.textContent = "Калькуляторы";
  dropdown.appendChild(summary);
  var panel = document.createElement("div");
  panel.appendChild(
    link(seasonPrefix + "distance_points_calculator.html", "Очки за дистанцию")
  );
  panel.appendChild(
    link(seasonPrefix + "rating_points_calculator.html", "Очки рейтинга")
  );
  dropdown.appendChild(panel);
  document.addEventListener("click", function (event) {
    if (!dropdown.contains(event.target)) dropdown.removeAttribute("open");
  });
  links.appendChild(dropdown);

  links.appendChild(link(seasonPrefix + "index.html", "Старты"));

  nav.appendChild(links);

  // close the mobile menu after picking a link
  links.addEventListener("click", function (event) {
    if (event.target.tagName === "A" && nav.classList.contains("open")) {
      nav.classList.remove("open");
      toggle.setAttribute("aria-expanded", "false");
    }
  });

  document.body.insertBefore(nav, document.body.firstChild);
})();
