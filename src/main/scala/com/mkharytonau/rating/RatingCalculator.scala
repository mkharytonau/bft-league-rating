package com.mkharytonau.rating

import com.mkharytonau.rating.domain._

trait RatingCalculator {
  def calculate(
      licenses: List[License],
      competitions: List[CompetitionCalculated]
  ): Rating
}

object RatingCalculator {
  object Standart2025 extends RatingCalculator {
    def calculate(
        licenses: List[License],
        competitions: List[CompetitionCalculated]
    ): Rating = {
      val events = competitions.flatMap(_.events)
      val header = Header(
        (List(
          "Место",
          "ФИО",
          "Номер лицензии",
          "Год рождения",
          "Клуб"
        ) ++ events
          .map(_.name.value) ++ List("Сумма очков")).map(ColumnName(_))
      )

      val ratingRows = licenses.map { license =>
        val eventsPoints: List[EventPoints] = competitions.map { competition =>
          competition.events.map { eventCalculated =>
            val licenseResults = eventCalculated.results.calculated
              .flatMap(_.calculation)
              .filter(_.license == license)
            require(licenseResults.size <= 1)
            val licenseResult = licenseResults.headOption
            EventPoints(eventCalculated.name, licenseResult.map(_.points))
          }
        }.flatten // TODO do we need to only keep one event per competition?
        val totalPoints =
          Points(eventsPoints.map(_.pointsMaybe.map(_.value).getOrElse(0d)).sum)
        (license, eventsPoints, totalPoints)
      }

      val ratingRowsSorted = ratingRows
        .sortBy { case (_, _, totalPoints) => -totalPoints.value }
        .zipWithIndex
        .map { case ((license, eventsPoints, totalPoints), index) =>
          RatingRow(Place(index + 1), license, eventsPoints, totalPoints)
        }

      Rating(header, ratingRowsSorted)
    }
  }
}
