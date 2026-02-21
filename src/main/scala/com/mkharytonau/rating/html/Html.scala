package com.mkharytonau.rating.html

import scalatags.Text.all._
import scalatags.Text.TypedTag
import scalatags.Text.tags2.{style, title}
import com.mkharytonau.rating.domain.EventConfig

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
      rows: Seq[(Option[Double], Seq[String])]
  ): TypedTag[String] =
    table(
      thead(
        tr(header.map(th(_)))
      ),
      tbody(
        rows.map { case (gradientPct, row) =>
          tr(attr("style") := s"--bar:${gradientPct.getOrElse(0)}%")(
            row.map(td(_))
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
          "❗ По всем вопросам, пожалуйста, обращайтесь в телеграм @mkharytonau"
        ),
        resultsTable
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
          "❗ По всем вопросам, пожалуйста, обращайтесь в телеграм @mkharytonau"
        ),
        ratingTable
      )
    )
}
