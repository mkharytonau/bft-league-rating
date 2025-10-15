package com.mkharytonau.rating

import com.mkharytonau.rating.domain._
import com.mkharytonau.rating.domain.Categories.ByLicense

object StatisticsCaclulator {
  def calculate(
      licenses: List[License],
      competitions: List[CompetitionCalculated],
      rating: Rating
  ): Statistics = {
    val unique = UniqueStatistics(
      licenses = licenses.distinct.size
    )

    val eventsStatistics =
      competitions.flatMap(_.events).map { eventCalculated =>
        val participants = eventCalculated.results.calculated.size
        val licensed =
          eventCalculated.results.calculated.count(_.calculation.isDefined)
        val unlicensed = participants - licensed
        val categories = Categories(
          license = ByLicense(
            licensed = licensed,
            unlicensed = unlicensed
          ),
          ageGroup = eventCalculated.results.calculated
            .flatMap(_.calculation)
            .groupBy(_.license.ag)
            .view
            .mapValues(_.size)
            .toMap,
          club = eventCalculated.results.calculated
            .flatMap(_.calculation)
            .groupBy(_.license.club.orElse(Club.fromString("Без клуба")).get)
            .view
            .mapValues(_.size)
            .toMap
        )

        EventStatistics(
          eventCalculated.name,
          participants,
          categories
        )
      }

    val totalStatistics = TotalStatistics(
      participants = eventsStatistics.map(_.participants).sum,
      categories = Categories(
        license = ByLicense(
          licensed = eventsStatistics.map(_.categories.license.licensed).sum,
          unlicensed = eventsStatistics.map(_.categories.license.unlicensed).sum
        ),
        ageGroup = eventsStatistics
          .flatMap(_.categories.ageGroup)
          .groupBy(_._1)
          .view
          .mapValues(_.map(_._2).sum)
          .toMap,
        club = eventsStatistics
          .flatMap(_.categories.club)
          .groupBy(_._1)
          .view
          .mapValues(_.map(_._2).sum)
          .toMap
      )
    )

    val participations = rating.rows.map(r =>
      r.license.fioInRussian -> r.eventsPoints.count(_.pointsMaybe.isDefined)
    )
    val bestParticipants = participations.sortBy(-_._2).take(5).map {
      case (fioInRussian, participations) =>
        AmountOfParticipations.BestParticipant(fioInRussian, participations)
    }
    val histogram: Map[Int, Int] =
      participations.groupBy(_._2).view.mapValues(_.size).toMap
    val amountOfParticipations =
      AmountOfParticipations(histogram, bestParticipants)

    Statistics(
      unique,
      totalStatistics,
      eventsStatistics,
      amountOfParticipations
    )
  }
}
