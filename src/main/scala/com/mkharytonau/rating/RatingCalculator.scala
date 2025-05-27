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
          "№",
          "▼▲",
          "ФИО",
          "Клуб",
          "Год рождения",
          "AG"
        ) ++ events
          .map(_.name.value) ++ List("Сумма очков", "Место в AG"))
          .map(ColumnName(_))
      )

      val ratingRowsPrevious = ratingRows(licenses, competitions.dropRight(1))
      val ratingRowsCurrent = ratingRows(licenses, competitions)

      val ratingRowsWithTrend = ratingRowsCurrent.map {
        case (place, license, eventsPoints, totalPoints, placeAG) =>
          val trend =
            if (competitions.dropRight(1).nonEmpty)
              ratingRowsPrevious.collectFirst {
                case (placePrevious, licensePrevious, _, _, _)
                    if licensePrevious == license =>
                  Trend(place.value - placePrevious.value)
              }.get // licenses list is the same so it MUST present
            else Trend(0)
          RatingRow(place, trend, license, eventsPoints, totalPoints, placeAG)
      }

      Rating(header, ratingRowsWithTrend)
    }

    def ratingRows(
        licenses: List[License],
        competitions: List[CompetitionCalculated]
    ) = {
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

      val indexInAG = ratingRows
        .groupBy { case (license, _, _) => license.ag }
        .view
        .mapValues { rows =>
          rows
          .map { case (_, _, totalPoints) => totalPoints }
          .toList.distinct.sortBy(-_.value).zipWithIndex.toMap
        }

      val ratingRowsWithPlaces = ratingRows
        .groupBy { case (_, _, totalPoints) => totalPoints }
        .toList
        .sortBy { case (totalPoints, _) => -totalPoints.value }
        .zipWithIndex
        .flatMap { case ((_, rows), index) =>
          rows.map { case (license, eventsPoints, totalPoints) =>
            (Place(index + 1), license, eventsPoints, totalPoints, Place(indexInAG(license.ag)(totalPoints) + 1))
          }
        }

      ratingRowsWithPlaces
    }
  }
}
