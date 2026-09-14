package com.mkharytonau.rating.html

import scalatags.Text.all._
import scalatags.Text.TypedTag
import scalatags.Text.tags2.{style, title}
import com.mkharytonau.rating.domain.EventConfig

object Html {

  def commonHead(pageTitle: String, stylesHref: String) = {
    val scriptHref = stylesHref.replace("styles.css", "table-tools.js")
    val navScriptHref = stylesHref.replace("styles.css", "nav.js")
    head(
      link(rel := "preconnect", href := "https://fonts.googleapis.com"),
      link(
        rel := "preconnect",
        href := "https://fonts.gstatic.com",
        attr("crossorigin") := ""
      ),
      link(
        href := "https://fonts.googleapis.com/css2?family=Roboto+Flex:opsz,wght@8..144,100..1000&display=swap",
        rel := "stylesheet"
      ),
      link(
        href := stylesHref,
        rel := "stylesheet"
      ),
      meta(charset := "UTF-8"),
      meta(
        name := "viewport",
        content := "width=device-width, initial-scale=1.0"
      ),
      title(pageTitle),
      script(src := navScriptHref, attr("defer") := ""),
      script(src := scriptHref, attr("defer") := "")
    )
  }

  /** Wraps a table with a search box that table-tools.js wires up to filter
    * rows, and tags the table so table-tools.js makes its columns sortable
    * and the narrow-screen CSS turns its rows into cards.
    */
  def tableWithToolbar(dataTable: TypedTag[String]): TypedTag[String] =
    div(
      div(cls := "table-toolbar")(
        input(
          `type` := "search",
          cls := "table-search",
          attr("placeholder") := "Поиск…",
          attr("aria-label") := "Поиск по таблице"
        )
      ),
      dataTable
    )

  def resultsTable(
      header: Seq[String],
      rows: Seq[(Option[Double], Seq[String])]
  ): TypedTag[String] =
    table(cls := "enhanced-table")(
      thead(
        tr(header.map(th(_)))
      ),
      tbody(
        rows.map { case (gradientPct, row) =>
          tr(attr("style") := s"--bar:${gradientPct.getOrElse(0)}%")(
            row.zip(header).map { case (value, label) =>
              td(attr("data-label") := label)(value)
            }
          )
        }
      )
    )

  def resultsPage(
      resultsTable: TypedTag[String],
      eventConfig: EventConfig
  ) = "<!DOCTYPE html>" + {
    val styles =
      if (eventConfig.locatedInInnerFolder) "../../../../../../styles.css"
      else "../../../../../styles.css"
    val backUrl =
      if (eventConfig.locatedInInnerFolder) "../index.html" else "index.html"
    html(
      commonHead(eventConfig.name.ratingName, styles),
      body(
        a(href := backUrl, "← Все категории"),
        h1(eventConfig.name.ratingName),
        p(
          "❗ По всем вопросам, пожалуйста, обращайтесь в телеграм ",
          a(href := "https://t.me/mkharytonau", "@mkharytonau")
        ),
        tableWithToolbar(resultsTable)
      )
    )
  }

  def ratingPage(
      pageTitle: String,
      header: String,
      ratingTable: TypedTag[String]
  ) = "<!DOCTYPE html>" +
    html(
      commonHead(pageTitle, "../../../../styles.css"),
      body(
        a(href := "rating.html", "← Все категории"),
        h1(header),
        p(
          "❗ По всем вопросам, пожалуйста, обращайтесь в телеграм ",
          a(href := "https://t.me/mkharytonau", "@mkharytonau")
        ),
        tableWithToolbar(ratingTable)
      )
    )
}
