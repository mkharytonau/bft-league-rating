# AGENTS.md

Runbook for updating the BFT League Rating project with a new competition.

## Repo Shape

- Season data lives in `src/main/resources/<year>/`.
- Each competition has its own folder, for example `src/main/resources/2026/Duathlon_Drogichin/`.
- Combined event inputs use `results.csv` with a `Пол` column, usually `М` / `Ж`.
- Generated outputs are written next to `results.csv`:
  - `men_calculated.csv`
  - `men_calculated.html`
  - `women_calculated.csv`
  - `women_calculated.html`
- Season rating outputs live at:
  - `src/main/resources/<year>/rating_men.csv`
  - `src/main/resources/<year>/rating_men.html`
  - `src/main/resources/<year>/rating_women.csv`
  - `src/main/resources/<year>/rating_women.html`
  - `src/main/resources/<year>/statistics_men.json`
  - `src/main/resources/<year>/statistics_women.json`

## Processing A New Competition

1. Check the worktree first:

   ```bash
   git status --short
   ```

   Treat existing user edits as intentional. Do not revert unrelated changes.

2. Inspect existing competition configs in:

   ```text
   src/main/scala/com/mkharytonau/rating/Main.scala
   ```

   Match the local style for `CompetitionConfig`, `EventConfig`, `EventCategory`, parser choice, folder naming, and rating base.

3. Prepare the competition folder:

   ```text
   src/main/resources/<year>/<Competition_Folder>/
   ```

   Keep source result files there too, for example `.xlsx`.

4. Extract `results.csv`.

   For workbooks with separate men/women sheets, combine them into one CSV and add `Пол`.

   Required fields depend on `Main.scala`, but the common 2026 duathlon pattern is:

   ```scala
   EventResultsReader.Configured(
     "Фамилия Имя",
     ParseResult.HoursMinutesSecondsMillisOrTens("Время"),
     ParseGender.ByField("Пол", "М", "Ж")
   )
   ```

   That means `results.csv` must have:

   ```text
   Фамилия Имя
   Время
   Пол
   ```

   The `Время` value must be parseable as `H:MM:SS.t`, `H:MM:SS.tt`, or `H:MM:SS.ttt`. Rows like `DSQ`, `DNF`, and `DNS` should be excluded unless the reader/calculator is extended to support statuses.

5. Add or update the competition page:

   ```text
   src/main/resources/<year>/<Competition_Folder>/index.html
   ```

   Use existing event pages as templates. Link to:

   ```text
   men_calculated.html
   women_calculated.html
   ```

6. Add the competition to the season index:

   ```text
   src/main/resources/<year>/index.html
   ```

7. Add the competition config to `Main.scala`.

   Example:

   ```scala
   CompetitionConfig(
     name = CompetitionName("Дуатлон. Дрогичин"),
     events = List(
       EventConfig(
         EventName("Дуатлон. Дрогичин", "DuathlonDrogichin"),
         EventCategory.Duathlon,
         ResourcePath("2026/Duathlon_Drogichin"),
         EventResultsReader.Configured("Фамилия Имя", ParseResult.HoursMinutesSecondsMillisOrTens("Время"), ParseGender.ByField("Пол", "М", "Ж")),
         EventResultsCalculator.Standart,
         700.0
       )
     )
   )
   ```

   The `EventName` second argument is used in `rating_points_calculator.html` query params, so keep it stable and ASCII.

8. Run the generator:

   ```bash
   sbt "runMain com.mkharytonau.rating.Main"
   ```

9. Verify generated outputs:

   ```bash
   head -5 src/main/resources/<year>/<Competition_Folder>/men_calculated.csv
   head -5 src/main/resources/<year>/<Competition_Folder>/women_calculated.csv
   head -1 src/main/resources/<year>/rating_men.csv
   head -1 src/main/resources/<year>/rating_women.csv
   rg -n "<Competition Name>|<EventJsName>" src/main/resources/<year> src/main/scala/com/mkharytonau/rating/Main.scala
   ```

   Confirm the new event appears in rating headers and generated HTML.

10. Review changed files:

    ```bash
    git status --short
    ```

    Expected changes usually include:

    - New competition folder files.
    - `Main.scala`.
    - Season `index.html`.
    - Rating CSV/HTML files.
    - Statistics JSON files.

## Name Matching

The calculator awards points only to athletes matched to licenses. Matching is handled in:

```text
src/main/scala/com/mkharytonau/rating/EventResultsCalculator.scala
src/main/resources/<year>/name_mapping.csv
src/main/resources/<year>/licenses.csv
```

If an athlete in `results.csv` has no calculated place/points but should be licensed, check whether the result name matches the license name or needs an entry in `name_mapping.csv`.

## Useful Patterns

- Use `rg` / `rg --files` for searching.
- Use `unzip -p <file.xlsx> xl/workbook.xml` to inspect workbook sheet names when a converter is unavailable.
- In `.xlsx`, workbook relationships map sheet names to `xl/worksheets/sheetN.xml`.
- Generated files can change for existing events if `licenses.csv` changed.
