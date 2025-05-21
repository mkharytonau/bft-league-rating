package com.mkharytonau.rating

import com.mkharytonau.rating.domain._
import com.github.tototoshi.csv.CSVWriter
import com.mkharytonau.rating.html.Html

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
          license.id.value,
          license.birthday.value,
          license.club.map(_.value).getOrElse("")
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

      val header = rating.header.value
        .map(_.value)

      val rows = rating.rows.map { ratingRow =>
        val license = ratingRow.license
        val row = List(
          ratingRow.place.value.toString,
          ratingRow.trend.show,
          license.fioInRussian.value,
          license.id.value,
          license.birthday.value,
          license.club.map(_.value).getOrElse("")
        ) ++ ratingRow.eventsPoints.map(
          _.pointsMaybe.map(_.value.toString).getOrElse("")
        ) ++ List(f"${ratingRow.totalPoints.value}%.2f")

        row
      }

      val htmlString = Html.ratingPage(
        "Рейтинг Любительской Лиги триатлона",
        "Рейтинг Любительской Лиги триатлона",
        Html.ratingTable(header, rows)
      )

      writer.write(htmlString)
      writer.close()
    }
  }
}
