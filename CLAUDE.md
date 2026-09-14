# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## What this is

A Scala batch program that computes the "BFT Amateur League" (Любительская Лига) triathlon
rating for Belarus. It reads per-event race results and a season license/roster CSV, matches
athletes to licenses, scores each event, aggregates points into an overall rating, and writes the
results back out as CSV, static HTML, and JSON. The generated `src/main/resources/<season>/**`
tree (plus the root `index.html`/`styles.css`/`nav.js`/`table-tools.js`) *is* the published static
site — the whole repo is served as-is (no server, no JS build step). Current season is `2026`; a
`2025` archive tree exists alongside it with the same layout.

## Prerequisites

Neither Java nor sbt is guaranteed to be preinstalled. If `sbt`/`java` aren't found:

```
brew install openjdk sbt
export PATH="/opt/homebrew/opt/openjdk/bin:$PATH"   # openjdk is keg-only on macOS Homebrew
```

## Commands

- `sbt compile` — compile the project.
- `sbt test` — run tests (munit). Only a placeholder test exists today (`HelloSpec`).
- `sbt "testOnly example.HelloSpec"` — run a single test suite.
- `sbt "runMain com.mkharytonau.rating.Main"` — regenerate event results, rating, and statistics
  for both genders for the current season (`Main` loops `List(Men, Women)` internally — there is no
  more per-gender entry point).
- `sbt scalafmtAll` — format sources (config in `.scalafmt.conf`, dialect scala213).

Writers resolve their output path via `domain.ResourcesDir.path(...)`, which is
`System.getProperty("user.dir") + "/src/main/resources/" + relativePath` — this works from any
checkout as long as you run it via `sbt` (which sets `user.dir` to the project root). Don't
hardcode absolute paths in a writer; use `ResourcesDir.path`.

## Architecture

Everything is scored through one pipeline, run once per gender inside **`Main.scala`**, which
inlines every `CompetitionConfig`/`EventConfig` for the season (~15+ competitions for 2026):

1. **`domain.scala`** — the shared model, using `io.estatico.newtype` value classes
   (`Nickname`, `LicenseId`, `Place`, `Points`, etc.). Key shapes:
   - `License` — one season's roster entry (id, RU/EN name, gender, age-group `AG`, club).
   - `EventResult` — one athlete's raw finish (`Nickname` as it appears in the results file,
     `FiniteDuration`, plus the full raw CSV row in `rawFields` for pass-through columns).
   - `EventConfig` — one event's raw results folder, `EventCategory` (`Sprint`/`Stayer`/
     `Duathlon`/`Multi` — used by the rating rule, see below), the `EventResultsReader` to parse
     it, a `ratingBase` (max points for the category winner), `locatedInInnerFolder` (event lives
     one directory deeper, e.g. `MinskTriathlon/sprint`, affecting generated relative links), and
     an optional `qualificationConfig` for two-stage events (a qualifying round feeding a final).
   - `RatingBreakdown` — which events count towards an athlete's total, split the way the rating
     rule actually works: `priorityByCategory` (best result per `EventCategory`, in a fixed order)
     plus `otherCounting` (the best few additional results). Computed once by
     `RatingCalculator`, consumed both for the point total and for the rating page's per-athlete
     expandable detail — don't recompute "which events count" elsewhere; use
     `RatingBreakdown.counting`/`.countingEventNames`.
   - `Statistics`/`Categories`/`EventStatistics`/etc. — circe-encoded (`@derive(encoder)`) model
     for the season dashboard, serialized straight to JSON.

2. **`EventResultsReader.Configured`** — a single configurable reader (not one object per
   source format): give it the nickname column, a `ParseResult` strategy
   (`HoursMinutesSecondsMillisOrTens(resultColumn)` or `IndoorTriathlon`) for the time format, and
   a `ParseGender` strategy (`ByField(column, menValue, womenValue)`). Reads from
   `<eventConfig.resultsPath>/results.csv` by convention. Adding a new results export format means
   adding a new `ParseResult`/`ParseGender` case, not a new reader object.

3. **`NameMatcher`** — fuzzy-matches a results-file nickname against a license's RU/EN full name
   or "surname firstname"/"firstname surname" via ICU4J Cyrillic→Latin transliteration + ASCII
   normalization + Jaro-Winkler similarity (threshold `0.97`), used by
   `EventResultsCalculator.findLicense`. `NameMapping` (`name_mapping.csv`, `;`-delimited) is
   still the last-resort fallback for aliases that don't fuzzy-match.

4. **`EventResultsCalculator`** — `Standart` ranks matched results by finish time and computes
   points relative to the category winner (`ratingBase * max(1 - (result-winner)/0.9/winner, 0)`).
   `FinalWithQualification` wraps it for two-stage events: finalists below the qualifying cutoff
   get their points clamped up to whatever the last qualifier who *didn't* make the final scored,
   so failing to qualify can't score you fewer points than qualifying-but-not-finishing would have.

5. **`EventResultsWriter`** (CSV/HTML) — writes one event's scored results, preserving the
   original CSV's columns and appending `Место`/`Очки в рейтинг`.

6. **`RatingCalculator.Standart2026`** — implements the actual league rule (p.14.7 of the 2026
   rules PDF): one automatic slot per `EventCategory` (best Sprint + best Stayer + best Duathlon +
   best Multi) plus the best 3 (men) / 1 (women) additional results, exposed as `RatingBreakdown`.
   Also computes overall place, per-age-group place (top-3 overall are exempt —
   `placeAG: Option[Place]`, `None` for them), and trend vs. the rating computed with the last
   competition dropped.

7. **`RatingWriter.HTML`** — renders **collapsed** summary rows (rank/avatar+name/club/AG/
   AG-place/total — not one column per event) with a per-athlete expandable detail row grouped
   into "Лучшие по категориям" / "Лучшие остальные (N)" / "Не учтено", using `RatingBreakdown`.
   A category can have every event on record be a non-participation (`EventPoints(..., pointsMaybe
   = None)`); render that as "не участвовал", not a stray chip — check `pointsMaybe.isDefined`,
   not just the `Option` around the whole `EventPoints`. Medal emoji (🥇🥈🥉) and 🚀 (best-improved
   rank) are rendered as cell *content*, so cells that need to be sortable by `table-tools.js`
   (rank, trend, AG-place) carry an explicit `data-sort` attribute with the real value.

8. **`StatisticsCaclulator`/`StatisticsWriter`** — computes participation/demographic stats
   (by sex, license status, age group, club, per event, attendance histogram) and writes
   `statistics_<gender>.json`, consumed client-side by `statistics.html`'s Plotly.js charts.

9. **`html/Html.scala`** — shared scalatags page shell. `commonHead` wires up fonts, `styles.css`,
   and both `nav.js`/`table-tools.js` (paths derived by string-swapping the given stylesheet href,
   so getting the stylesheet's relative depth right is what matters). `tableWithToolbar` wraps a
   table with the search box.

10. **`MT.scala`** — a one-off data-migration script (reshapes an old MinskTriathlon export into
    per-category CSVs), not invoked by `Main` and not part of the regular pipeline.

### Frontend layer (nav.js / table-tools.js / styles.css)

These three repo-root files are shared by every page, generated or hand-authored:

- **`nav.js`** — injects the persistent `Рейтинг / Статистика / Калькуляторы / Старты` nav as the
  first element of `<body>`. It finds the season folder by looking for a 4-digit path segment in
  `location.pathname` (not by counting relative-path depth), so it's robust to the repo's own
  `src/main/resources/<season>/...` nesting and works the same for the `2025` archive and the
  current `2026` season. Collapses into a "☰ Меню" burger below 640px.
- **`table-tools.js`** — for any `table.enhanced-table` preceded by a `.table-toolbar` search box:
  click-to-sort (numeric-aware; prefers a cell's `data-sort` attribute over its `textContent`),
  live text search, and click-to-expand/collapse for rows with a sibling `.detail-row` (the rating
  page's per-athlete breakdown). Blank headers (e.g. the expand-chevron column) are skipped.
- **`styles.css`** — the whole visual theme (CSS custom properties for the palette, sticky nav,
  table styling). Two mobile card layouts coexist: a generic label/value stacking (driven by each
  `td`'s `data-label`) for event-results tables' arbitrary columns, and a bespoke compact 2-line
  flex layout specifically for `.rating-table` rows (forcing the rating row's fixed field set
  through the generic stacking looked bad — one field per line, one screen per athlete).
- Hand-authored pages (root `index.html`, each season's `index.html`/`rating.html`/
  `statistics.html`/the two calculator pages, and every event's `index.html`) aren't generated by
  Scala — each has its own manually-added `<script src="...nav.js" defer></script>` tag matching
  that file's relative depth. Copy the pattern from a sibling file at the same nesting depth when
  adding a new one.

### Adding a new event/competition

Edit `Main.scala`'s `competitionConfigs` list to add a `CompetitionConfig`/`EventConfig` pointing
at `src/main/resources/<season>/<Event>/results.csv`, picking the matching `ParseResult`/
`ParseGender` for its export format, an `EventCategory`, and a `ratingBase`. Then add a matching
`<Event>/index.html` (copy an existing sibling event's, including its `nav.js` script tag), and the
raw `results.csv`.

### Avatars

`src/main/resources/<season>/img/avatars/handle_images.py` (standalone Python/Pillow script, not
part of the sbt build) normalizes raw avatar photos in `./raw` into 64x64 center-cropped JPEGs in
`./thumbnails`, keyed by the athlete's RU FIO filename — this is what `RatingWriter.HTML`
references via `img/avatars/thumbnails/${license.fioInRussian.value}.jpg`.
