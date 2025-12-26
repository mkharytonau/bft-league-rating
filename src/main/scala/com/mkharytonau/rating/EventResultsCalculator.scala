package com.mkharytonau.rating

import com.mkharytonau.rating.domain._
import com.mkharytonau.rating.domain.License.FIOInRussian
import com.mkharytonau.rating.domain.License.FIOInEnglish
import scala.concurrent.duration.FiniteDuration

trait EventResultsCalculator[Context, A] {
  def calculate(
      results: EventResults,
      eventConfig: EventConfig[Context, A],
      licenses: List[License],
      nameMapping: Map[FIOInRussian, List[Nickname]],
      context: Context
  ): EventResultsCalculated[A]
}

object EventResultsCalculator {

  def findLicense(
      result: EventResult,
      licenses: List[License],
      nameMapping: Map[FIOInRussian, List[Nickname]]
  ): Option[License] = {
    val byFIInRussian = licenses.filter { license =>
      license.fioInRussian.value.toLowerCase
        .split(" ")
        .take(2)
        .map(_.trim) match {
        case Array(surname, name) =>
          s"$surname $name" == result.nickname.value.toLowerCase || // both Харитонов Никита
          s"$name $surname" == result.nickname.value.toLowerCase // and Никита Харитонов match
      }
    } // FI in russian present in results, the most probable case
    require(byFIInRussian.size <= 1)
    val byFIOInRussian = licenses.filter(
      _.fioInRussian.value.toLowerCase == result.nickname.value.toLowerCase
    ) // FIO in russian present in results, less probable case
    require(byFIOInRussian.size <= 1)
    val byFIOInEnglish = licenses.filter { license =>
      license.fioInEnglish.value.toLowerCase
        .split(" ")
        .take(2)
        .map(_.trim) match {
        case Array(surname, name) =>
          s"$surname $name" == result.nickname.value.toLowerCase || // both Kharitonov Nikita
          s"$name $surname" == result.nickname.value.toLowerCase // and Nikita Kharitonov match
      }
    } // FIO in english present in results, less probable case
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
    val byAll = byFIInRussian ++ byFIOInRussian ++ byFIOInEnglish ++ byNickname
    if (byAll.size > 1) {
      println(s"byFIInRussian: $byFIInRussian")
      println(s"byFIOInRussian: $byFIOInRussian")
      println(s"byFIOInEnglish: $byFIOInEnglish")
      println(s"byNickname: $byNickname")
    }
    require(byAll.size <= 1)
    byAll.headOption
  }

  object Standart extends EventResultsCalculator[Any, Unit] {

    def calculate(
        results: EventResults,
        eventConfig: EventConfig[Any, Unit],
        licenses: List[License],
        nameMapping: Map[FIOInRussian, List[Nickname]],
        context: Any
    ): EventResultsCalculated[Unit] = {
      val sorted = results.results
        .map(result => (result, findLicense(result, licenses, nameMapping)))
        .sortBy { case (result, _) => result.result }
      val licensedAndSorted = sorted.collect { case (res, Some(lic)) =>
        (res, lic)
      }
      val licensedWinnerResultMaybe = licensedAndSorted.headOption.map {
        case (result, _) => result
      }

      val calculated = sorted.zipWithIndex.map {
        case ((result, licenseMaybe), index) =>
          licensedWinnerResultMaybe.foreach { licensedWinnerResult =>
            val points2 = calculatePoints(
              result.result,
              licensedWinnerResult.result,
              eventConfig.ratingBase
            )
            def formatTime(duration: FiniteDuration): String = {
              val totalSeconds = duration.toSeconds
              val hours = totalSeconds / 3600
              val minutes = (totalSeconds % 3600) / 60
              val seconds = totalSeconds % 60
              f"$hours%02d:$minutes%02d:$seconds%02d"
            }

            if (points2.value < 0.1) {
              println(s"Очков: ${points2.value}, ФИО: ${result.nickname.value}, Время: ${formatTime(result.result)}, Время победителя: ${formatTime(
                  licensedWinnerResult.result
                )}, Место: ${index + 1} из ${sorted.size}, Старт: ${eventConfig.name.ratingName}")
            }
          }

          EventResultWithCalculation(
            result,
            calculation = licenseMaybe.flatMap { license =>
              licensedWinnerResultMaybe.map { licensedWinnerResult =>
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
            }
          )
      }

      EventResultsCalculated(results, calculated)
    }

    def calculatePoints(
        athleteResult: FiniteDuration,
        championResult: FiniteDuration,
        competitionPoints: Double
    ): Points = {
      val points = competitionPoints * scala.math.max(
        (1 - (athleteResult - championResult) / 0.9 / championResult),
        0
      )
      val pointsRounded =
        BigDecimal(points).setScale(2, BigDecimal.RoundingMode.HALF_UP).toDouble
      Points(pointsRounded)
    }
  }
}
