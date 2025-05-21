package com.mkharytonau.rating.html

import scalatags.Text.all._
import scalatags.Text.TypedTag
import scalatags.Text.tags2.{style, title}

object Html {

  def commonHead(pageTitle: String, stylesHref: String) =
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
      title(pageTitle)
    )

  def resultsTable(
      header: Seq[String],
      rows: Seq[Seq[String]]
  ): TypedTag[String] =
    table(
      thead(
        tr(header.map(th(_)))
      ),
      tbody(
        rows.map { row =>
          tr(row.map(td(_)))
        }
      )
    )

  def ratingTable(
      header: Seq[String],
      rows: Seq[Seq[String]]
  ): TypedTag[String] =
    table(
      thead(
        tr(header.map(th(_)))
      ),
      tbody(
        rows.map { row =>
          tr(row.map { cell =>
            cell.headOption match {
              case Some('▲') => td(cls := "green")(cell)
              case Some('▼') => td(cls := "red")(cell)
              case Some('−') => td(cls := "yellow")(cell)
              case _         => td(cell)
            }
          })
        }
      )
    )

  def resultsPage(
      pageTitle: String,
      header: String,
      resultsTable: TypedTag[String]
  ) = "<!DOCTYPE html>" +
    html(
      commonHead(pageTitle, "../../../../../styles.css"),
      body(
        a(href := "index.html", "← Все категории"),
        h1(header),
        resultsTable
      )
    )

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
        ratingTable
      )
    )
}
