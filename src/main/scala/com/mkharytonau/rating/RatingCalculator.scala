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
          "AG",
          "Место в AG"
        ) ++ events
          .map(_.name.ratingName) ++ List("Сумма очков"))
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
          RatingRow(place, trend, license, placeAG, eventsPoints, totalPoints)
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
            EventPoints(
              eventCalculated.name,
              eventCalculated.eventCategory,
              licenseResult.map(_.points)
            )
          }
        }.flatten // TODO do we need to only keep one event per competition?
        val totalPoints = calculateTotalPoints(license, eventsPoints)
        (license, eventsPoints, totalPoints)
      }

      val indexInAG = ratingRows
        .groupBy { case (license, _, _) => license.ag }
        .view
        .mapValues { rows =>
          rows
            .map { case (_, _, totalPoints) => totalPoints }
            .toList
            .distinct
            .sortBy(-_.value)
            .zipWithIndex
            .toMap
        }

      val ratingRowsWithPlaces = ratingRows
        .groupBy { case (_, _, totalPoints) => totalPoints }
        .toList
        .sortBy { case (totalPoints, _) => -totalPoints.value }
        .zipWithIndex
        .flatMap { case ((_, rows), index) =>
          rows.map { case (license, eventsPoints, totalPoints) =>
            (
              Place(index + 1),
              license,
              eventsPoints,
              totalPoints,
              Place(indexInAG(license.ag)(totalPoints) + 1)
            )
          }
        }

      ratingRowsWithPlaces
    }

    def calculateTotalPoints(
        license: License,
        eventsPoints: List[EventPoints]
    ): Points = {
      // see p.14.8 of https://triatlon.by/assets/images/files/2025/polozhenie-lyubitelskaya-liga-triatlona-2025.pdf
      val otherCount = 4 // 14 * 0.6 = 8.4 (округляем до 8) - 4 = 4

      val bestSprintMaybe = eventsPoints
        .filter(_.eventCategory == EventCategory.Sprint)
        .sortBy(-_.pointsMaybe.map(_.value).getOrElse(0d))
        .headOption

      val bestOlympicMaybe = eventsPoints
        .filter(_.eventCategory == EventCategory.Olympic)
        .sortBy(-_.pointsMaybe.map(_.value).getOrElse(0d))
        .headOption

      val bestDuathlonMaybe = eventsPoints
        .filter(_.eventCategory == EventCategory.Duathlon)
        .sortBy(-_.pointsMaybe.map(_.value).getOrElse(0d))
        .headOption

      val bestKrossMaybe = eventsPoints
        .filter(_.eventCategory == EventCategory.Kross)
        .sortBy(-_.pointsMaybe.map(_.value).getOrElse(0d))
        .headOption

      val takenWithPriority = List(
        bestSprintMaybe,
        bestOlympicMaybe,
        bestDuathlonMaybe,
        bestKrossMaybe
      ).flatten.map(eventsPoints =>
        (eventsPoints.eventName, eventsPoints.pointsMaybe)
      )

      require(
        eventsPoints
          .map(ep => (ep.eventName, ep.pointsMaybe))
          .distinct
          .size == eventsPoints.size,
        "There are duplicate event names with points in the eventsPoints list"
      )
      val other = eventsPoints
        .filterNot(ep =>
          takenWithPriority.exists { case (name, points) =>
            ep.eventName == name && ep.pointsMaybe == points
          }
        )
        .sortBy(-_.pointsMaybe.map(_.value).getOrElse(0d))
        .take(otherCount)

      val bestSprint =
        bestSprintMaybe.flatMap(_.pointsMaybe).getOrElse(Points(0d)).value
      val bestOlympic =
        bestOlympicMaybe.flatMap(_.pointsMaybe).getOrElse(Points(0d)).value
      val bestDuathlon =
        bestDuathlonMaybe.flatMap(_.pointsMaybe).getOrElse(Points(0d)).value
      val bestKross =
        bestKrossMaybe.flatMap(_.pointsMaybe).getOrElse(Points(0d)).value
      val otherPoints = other.flatMap(_.pointsMaybe).map(_.value).sum

      println(
        s"${license.fioInRussian.value} -- " +
          s"Sprint: ${bestSprintMaybe.map(a => (a.eventName.ratingName, a.pointsMaybe))}, " +
          s"Olympic: ${bestOlympicMaybe.map(a => (a.eventName.ratingName, a.pointsMaybe))}, " +
          s"Duathlon: ${bestDuathlonMaybe.map(a => (a.eventName.ratingName, a.pointsMaybe))}, " +
          s"Kross: ${bestKrossMaybe.map(a => (a.eventName.ratingName, a.pointsMaybe))}, " +
          s"Other: ${other.map(ep => (ep.eventName.ratingName, ep.pointsMaybe))}"
      )

      val totalPoints =
        bestSprint + bestOlympic + bestDuathlon + bestKross + otherPoints
      Points(totalPoints)
    }
  }
}
