package com.mkharytonau.rating

import com.mkharytonau.rating.domain._
import com.mkharytonau.rating.domain.License.FIOInRussian
import com.mkharytonau.rating.domain.License.FIOInEnglish
import scala.concurrent.duration.FiniteDuration

trait EventResultsCalculator[Context, A] {
  def calculate(
      results: List[EventResult],
      eventConfig: EventConfig,
      licenses: List[License],
      nameMapping: Map[FIOInRussian, List[Nickname]],
      context: Context
  ): List[EventResultWithCalculation[A]]
}

object EventResultsCalculator {

  def findLicense(
      result: EventResult,
      licenses: List[License],
      nameMapping: Map[FIOInRussian, List[Nickname]]
  ): Option[License] = {
    val byFIOInRussian = licenses.filter(
      _.fioInRussian == FIOInRussian(result.nickname.value)
    ) // FIO in russian present in results, the most probable case
    require(byFIOInRussian.size <= 1)
    val byFIOInEnglish = licenses.filter(
      _.fioInEnglish == FIOInEnglish(result.nickname.value)
    ) // FIO in english present in results, less probable case
    require(byFIOInEnglish.size <= 1)
    val byNickname = { // result contains custom nickname, try to map it to FIO in russian and find license for it
      val fiosInRussian = nameMapping.collect {
        case (fioInRussian, nicknames) if nicknames.contains(result.nickname) =>
          fioInRussian
      }.toList
      require(fiosInRussian.size <= 1)
      licenses.filter(lic => fiosInRussian.contains(lic.fioInRussian))
    }
    require(byNickname.size <= 1)
    val byAll = byFIOInRussian ++ byFIOInEnglish ++ byNickname
    require(licenses.size <= 1)
    byAll.headOption
  }

  object Standart extends EventResultsCalculator[Any, Unit] {

    def calculate(
        results: List[EventResult],
        eventConfig: EventConfig,
        licenses: List[License],
        nameMapping: Map[FIOInRussian, List[Nickname]],
        context: Any
    ): List[EventResultWithCalculation[Unit]] = {
      val sorted = results
        .map(result => (result, findLicense(result, licenses, nameMapping)))
        .sortBy { case (result, _) => result.result }
      sorted.map { case (result, licenseMaybe) =>
        EventResultWithCalculation(
          result,
          calculation = licenseMaybe.map { license =>
            val licensedAndSorted = sorted.collect { case (res, Some(lic)) =>
              (res, lic)
            }
            val (licensedWinnerResult, _) = licensedAndSorted.head
            val place = licensedAndSorted.indexWhere { case (_, lic) =>
              license == lic
            } + 1
            val points = calculatePoints(
              result.result,
              licensedWinnerResult.result,
              eventConfig.ratingBase
            )
            EventResultCalculation(license, Place(place), points, ())
          }
        )
      }
    }

    def calculatePoints(
        athleteResult: FiniteDuration,
        championResult: FiniteDuration,
        competitionPoints: Double
    ): Points =
      Points(
        competitionPoints * scala.math
          .max((1 - (athleteResult - championResult) / 0.8 / championResult), 0)
      )
  }
}
