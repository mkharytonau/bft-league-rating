package com.mkharytonau.rating

import com.mkharytonau.rating.domain._
import com.mkharytonau.rating.domain.Gender.Men
import com.mkharytonau.rating.domain.Gender.Women

trait RatingCalculator {
  def calculate(
      licenses: List[License],
      competitions: List[CompetitionCalculated]
  ): Rating
}

object RatingCalculator {
  object Standart2026 extends RatingCalculator {
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
          "AG",
          "Место в AG"
        ) ++ events
          .map(_.name.ratingName) ++ List("Сумма"))
          .map(ColumnName(_))
      )

      val ratingRowsPrevious = ratingRows(licenses, competitions.dropRight(1))
      val ratingRowsCurrent = ratingRows(licenses, competitions)

      val ratingRowsWithTrend = ratingRowsCurrent.map {
        case (place, license, eventsPoints, totalPoints, countingNames, placeAG) =>
          val trend =
            if (competitions.dropRight(1).nonEmpty)
              ratingRowsPrevious.collectFirst {
                case (placePrevious, licensePrevious, _, _, _, _)
                    if licensePrevious == license =>
                  Trend(place.value - placePrevious.value)
              }.get // licenses list is the same so it MUST present
            else Trend(0)
          (place, trend, license, placeAG, eventsPoints, totalPoints, countingNames)
      }
      val theBestTrend = ratingRowsWithTrend
        .map { case (_, trend, _, _, _, _, _) => trend.value }
        .minOption

      val ratingRows0 = ratingRowsWithTrend.map {
        case (place, trend, license, placeAG, eventsPoints, totalPoints, countingNames) =>
          RatingRow(
            place,
            trend,
            license,
            placeAG,
            eventsPoints,
            totalPoints,
            countingNames,
            theBestTrend.contains(trend.value) && trend.value < 0
          )
      }
      val winnerPoints = ratingRows0.headOption.map(_.totalPoints)

      Rating(header, ratingRows0, winnerPoints)
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
        val (totalPoints, countingNames) = calculateTotalPoints(license, eventsPoints)
        (license, eventsPoints, totalPoints, countingNames)
      }

      val ratingRowsWithIndex = ratingRows
        .groupBy { case (_, _, totalPoints, _) => totalPoints }
        .toList
        .sortBy { case (totalPoints, _) => -totalPoints.value }
        .zipWithIndex

      val indexInAG = ratingRowsWithIndex
        .filter { case (_, index) => index + 1 > 3 } // exclude top 3
        .flatMap { case ((_, rows), index) => rows }
        .groupBy { case (license, _, _, _) => license.ag }
        .view
        .mapValues { rows =>
          rows
            .map { case (_, _, totalPoints, _) => totalPoints }
            .toList
            .distinct
            .sortBy(-_.value)
            .zipWithIndex
            .toMap
        }

      val ratingRowsWithPlaces = ratingRowsWithIndex
        .flatMap { case ((_, rows), index) =>
          rows.map { case (license, eventsPoints, totalPoints, countingNames) =>
            val absolutePlace = Place(index + 1)
            (
              absolutePlace,
              license,
              eventsPoints,
              totalPoints,
              countingNames,
              Option.when(absolutePlace.value > 3)(
                Place(indexInAG(license.ag)(totalPoints) + 1)
              )
            )
          }
        }

      ratingRowsWithPlaces
    }

    private def countingEvents(
        license: License,
        eventsPoints: List[EventPoints]
    ): List[EventPoints] = {
      // see p.14.7 of https://triatlon.by/assets/images/files/federation/polozhenie-lyubitelskaya-liga-2026-podpisano.pdf
      val otherCount = license.gender match {
        case Men   => 3
        case Women => 1
      }

      def bestInCategory(cat: EventCategory): Option[EventPoints] =
        eventsPoints
          .filter(_.eventCategory == cat)
          .sortBy(-_.pointsMaybe.map(_.value).getOrElse(0d))
          .headOption

      val priorityBests = List(
        EventCategory.Sprint,
        EventCategory.Stayer,
        EventCategory.Duathlon,
        EventCategory.Multi
      ).flatMap(bestInCategory)

      val takenWithPriority = priorityBests.map(ep => (ep.eventName, ep.pointsMaybe))

      val other = eventsPoints
        .filterNot(ep =>
          takenWithPriority.exists { case (name, points) =>
            ep.eventName == name && ep.pointsMaybe == points
          }
        )
        .sortBy(-_.pointsMaybe.map(_.value).getOrElse(0d))
        .take(otherCount)

      priorityBests ++ other
    }

    def calculateTotalPoints(
        license: License,
        eventsPoints: List[EventPoints]
    ): (Points, Set[EventName]) = {
      require(
        eventsPoints
          .map(ep => (ep.eventName, ep.pointsMaybe))
          .distinct
          .size == eventsPoints.size,
        "There are duplicate event names with points in the eventsPoints list"
      )
      val counting = countingEvents(license, eventsPoints)
      val totalPoints = counting.flatMap(_.pointsMaybe).map(_.value).sum
      (Points(totalPoints), counting.map(_.eventName).toSet)
    }
  }
}
