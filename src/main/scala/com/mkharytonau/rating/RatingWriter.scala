package com.mkharytonau.rating

import com.mkharytonau.rating.domain._
import com.github.tototoshi.csv.CSVWriter

trait RatingWriter {
  def write(rating: Rating, path: ResourcePath): Unit
}

object RatingWriter {
  object Standart extends RatingWriter {
    def write(rating: Rating, path: ResourcePath): Unit = {
      val writer = CSVWriter.open(
        s"/Users/mkharytonau/Projects/bft-league-rating/src/main/resources/${path.value}" // TODO don't hardcode path to resources folder
      )

      writer.writeRow(rating.header.value.map(_.value))

      rating.rows.foreach { ratingRow =>
        val license = ratingRow.license
        val row = List(
          ratingRow.place.value.toString,
          license.fioInRussian.value,
          license.id.value,
          license.birthday.value,
          license.club.map(_.value).getOrElse("")
        ) ++ ratingRow.eventsPoints.map(
          _.pointsMaybe.map(_.value.toString).getOrElse("")
        ) ++ List(ratingRow.totalPoints.value.toString)

				writer.writeRow(row)
      }

      writer.close()
    }
  }
}
