package com.mkharytonau.rating

import com.mkharytonau.rating.domain._
import com.github.tototoshi.csv.CSVWriter
import com.mkharytonau.rating.html.Html
import scalatags.Text.all._
import scalatags.Text.TypedTag

trait RatingWriter {
  def write(rating: Rating, path: ResourcePath): Unit
}

object RatingWriter {
  object CSV extends RatingWriter {
    def write(rating: Rating, path: ResourcePath): Unit = {
      val writer = CSVWriter.open(
        s"/Users/mkharytonau/Projects/bft-league-rating/src/main/resources/${path.value}" // TODO don't hardcode path to resources folder
      )

      writer.writeRow(rating.header.value.map(_.value))

      rating.rows.foreach { ratingRow =>
        val license = ratingRow.license
        val row = List(
          ratingRow.place.value.toString,
          ratingRow.trend.show,
          license.fioInRussian.value,
          license.club.map(_.value).getOrElse(""),
          license.birthday.value,
          license.ag.show,
          ratingRow.placeAG.value.toString
        ) ++ ratingRow.eventsPoints.map(
          _.pointsMaybe.map(_.value.toString).getOrElse("")
        ) ++ List(f"${ratingRow.totalPoints.value}%.2f")

        writer.writeRow(row)
      }

      writer.close()
    }
  }

  object HTML extends RatingWriter {
    def write(rating: Rating, path: ResourcePath): Unit = {
      val filePath =
        s"/Users/mkharytonau/Projects/bft-league-rating/src/main/resources/${path.value}" // TODO don't hardcode path to resources folder
      val writer = new java.io.PrintWriter(filePath)

      val header = rating.header.value.map(_.value)
      val headerHtml = {
        val last = header.last
        header.init
          .map(th(_)) :+ th(
          span(last),
          raw("&nbsp;"),
          span(title := "Нажмите на очки, чтобы перейти в калькулятор и получить объяснения по формуле")("ℹ️")
        )
      }

      val rows = rating.rows.map { ratingRow =>
        val license = ratingRow.license
        val clubStr = license.club.map(_.value).getOrElse("")
        val jsCaluclatorPath = ratingRow.eventsPoints
          .map(eventPoints =>
            s"${eventPoints.eventName.jsCalculatorName}=${eventPoints.pointsMaybe.map(_.value.toString).getOrElse("")}"
          )
          .mkString("&")
        val totalPointsStr = f"${ratingRow.totalPoints.value}%.2f"
        val row = List(
          td(ratingRow.place.value.toString), {
            ratingRow.trend.show.headOption match {
              case Some('▲') => td(cls := "green")(ratingRow.trend.show)
              case Some('▼') => td(cls := "red")(ratingRow.trend.show)
              case Some('−') => td(cls := "yellow")(ratingRow.trend.show)
              case _         => td(ratingRow.trend.show)
            }
          },
          td(style := "white-space: nowrap;")(
            img(
              src := s"./img/avatars/thumbnails/${license.fioInRussian.value}.jpg",
              alt := "",
              style := "border-radius: 50%; max-height: 2em; max-width: 2em; vertical-align: middle;"
            ),
            raw("&nbsp;"),
            span(license.fioInRussian.value)
          ),
          td(clubStr),
          td(license.birthday.value),
          td(style := "white-space: nowrap;")(license.ag.show),
          td(ratingRow.placeAG.value.toString)
        ) ++ ratingRow.eventsPoints.map { eventPoints =>
          val pointsStr =
            eventPoints.pointsMaybe.map(_.value.toString).getOrElse("")
          td(pointsStr)
        } ++ List(
          td(
            a(
              href := s"./rating_points_calculator.html?$jsCaluclatorPath&scalaTotalValue=$totalPointsStr"
            )(totalPointsStr)
          )
        )

        tr(row)
      }

      val htmlTable = table(
        thead(
          tr(headerHtml)
        ),
        tbody(rows)
      )

      val htmlString = Html.ratingPage(
        "Рейтинг Любительской Лиги триатлона",
        "Рейтинг Любительской Лиги триатлона",
        htmlTable
      )

      writer.write(htmlString)
      writer.close()
    }
  }
}
