package com.mkharytonau.rating

import com.mkharytonau.rating.domain._
import com.mkharytonau.rating.domain.License.FIOInRussian
import com.mkharytonau.rating.domain.License.FIOInEnglish
import scala.concurrent.duration.FiniteDuration

trait EventResultsCalculator {
  def calculate(
      results: EventResults,
      eventConfig: EventConfig,
      licenses: List[License],
      nameMapping: Map[FIOInRussian, List[Nickname]]
  ): EventResultsCalculated
}

object EventResultsCalculator {

  def findLicense(
      result: EventResult,
      licenses: List[License],
      nameMapping: Map[FIOInRussian, List[Nickname]]
  ): Option[License] = {
    def firstTwoWords(value: String): Option[String] = {
      val words = value.trim.split("\\s+").take(2)
      Option.when(words.size == 2)(words.mkString(" "))
    }

    def nameCandidates(license: License): List[String] =
      List(
        Some(license.fioInRussian.value),
        firstTwoWords(license.fioInRussian.value),
        Some(license.fioInEnglish.value),
        firstTwoWords(license.fioInEnglish.value)
      ).flatten
        .filter(_.trim.nonEmpty)
        .distinct

    val byName = licenses.filter { license =>
      nameCandidates(license).exists { cand =>
        val matched = NameMatcher(cand, result.nickname.value)
        // if (b) println(s"nickname: ${result.nickname.value}, nameCandidate: $a, result: $b")
        matched
      }
    }
    require(
      byName.size <= 1,
      s"nickname: ${result.nickname.value}, byName: $byName"
    )

    val byNickname = { // result contains custom nickname, try to map it to FIO in russian and find license for it
      val fiosInRussian = nameMapping.collect {
        case (fioInRussian, nicknames) if nicknames.contains(result.nickname) =>
          fioInRussian
      }.toList
      require(fiosInRussian.size <= 1, s"fiosInRussian: $fiosInRussian")
      licenses.filter(lic => fiosInRussian.contains(lic.fioInRussian))
    }
    require(byNickname.size <= 1, s"byNickname: $byNickname")

    val byAll = (byName ++ byNickname).distinct
    if (byAll.size > 1) {
      println(s"byName: $byName")
      println(s"byNickname: $byNickname")
    }
    require(byAll.size <= 1, s"byAll: $byAll")
    byAll.headOption
  }

  object Standart extends EventResultsCalculator {

    def calculate(
        results: EventResults,
        eventConfig: EventConfig,
        licenses: List[License],
        nameMapping: Map[FIOInRussian, List[Nickname]]
    ): EventResultsCalculated = {
      val sorted = results.results
        .map(result => (result, findLicense(result, licenses, nameMapping)))
        .sortBy { case (result, _) => result.result }
      val licensedAndSorted = sorted.collect { case (res, Some(lic)) =>
        (res, lic)
      }
      val licensedWinnerResultMaybe = licensedAndSorted.headOption.map {
        case (result, _) => result
      }

      val calculated = sorted.map { case (result, licenseMaybe) =>
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
              EventResultCalculation(license, Place(place), points)
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

  class FinalWithQualification(qualificationResults: EventResultsCalculated)
      extends EventResultsCalculator {

    def calculate(
        results: EventResults,
        eventConfig: EventConfig,
        licenses: List[License],
        nameMapping: Map[FIOInRussian, List[Nickname]]
    ): EventResultsCalculated = {
      val finalCalculated =
        Standart.calculate(results, eventConfig, licenses, nameMapping)
      val finalistsAmount = finalCalculated.calculated.size
      val qualificationTail =
        qualificationResults.calculated.drop(finalistsAmount)
      val lowerBoundPoints =
        qualificationTail.flatMap(_.calculation.map(_.points)).headOption

      val finalRows = finalCalculated.calculated.map { row =>
        val clampedCalculation = row.calculation.map { calculation =>
          val points = lowerBoundPoints
            .map(lowerBound =>
              Points(math.max(calculation.points.value, lowerBound.value))
            )
            .getOrElse(calculation.points)

          calculation.copy(points = points)
        }

        row.copy(calculation = clampedCalculation)
      }

      EventResultsCalculated(
        results,
        finalRows ++ qualificationTail
      )
    }
  }
}
