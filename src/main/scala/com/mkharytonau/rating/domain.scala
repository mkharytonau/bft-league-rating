package com.mkharytonau.rating

import io.estatico.newtype.macros.newtype
import io.estatico.newtype.ops._
import com.mkharytonau.rating.domain.License.FIOInRussian
import com.mkharytonau.rating.domain.License.FIOInEnglish
import scala.concurrent.duration.FiniteDuration

object domain {

  @newtype case class Nickname(value: String)

  @newtype case class LicenseId(value: String) // unique for one season

  @newtype class Club(val value: String)
  object Club {
    def fromString(str: String): Option[Club] = {
      val trimmed = str.trim()
      if (trimmed.nonEmpty) Some(trimmed.coerce) else None
    }
  }
  @newtype case class Birthday(value: String)

  final case class License(
      id: LicenseId,
      fioInRussian: FIOInRussian,
      fioInEnglish: FIOInEnglish,
      gender: Gender,
      club: Option[Club],
      birthday: Birthday
  )
  object License {
    @newtype case class FIOInRussian(value: String)
    @newtype case class FIOInEnglish(value: String)
  }

  final case class LicensedAthlete(athlete: Nickname, license: License)

  @newtype case class ResourcePath(value: String)

  final case class EventResult(
      nickname: Nickname,
      result: FiniteDuration,
      rawFields: Map[String, String]
  )

  @newtype case class ColumnName(value: String)
  @newtype case class Header(value: List[ColumnName])
  final case class EventResults(header: Header, results: List[EventResult])

  @newtype case class Place(value: Int)
  @newtype case class Points(value: Double)

  final case class EventResultCalculation[A](
      license: License,
      place: Place,
      points: Points,
      additionalCalculation: A
  )

  final case class EventResultWithCalculation[A](
      result: EventResult,
      calculation: Option[EventResultCalculation[A]]
  )

  final case class EventResultsCalculated[A](
      results: EventResults,
      calculated: List[EventResultWithCalculation[A]]
  )

  final case class EventConfig[C, A](
    name: EventName,
      resultsPath: ResourcePath,
      resultsLoader: EventResultsReader,
      calculatedPath: ResourcePath,
      resultCalculator: EventResultsCalculator[C, A],
      ratingBase: Double
  )
  final case class CompetitionConfig[C, A](name: CompetitionName, events: List[EventConfig[C, A]])

  // rating
  @newtype case class CompetitionName(value: String)
  @newtype case class EventName(value: String)
  final case class EventCalculated[A](
    name: EventName,
      results: EventResultsCalculated[Unit]
  )
  final case class CompetitionCalculated(
      events: List[EventCalculated[Unit]]
  )

  @newtype case class Trend(value: Int) {
    def show: String = if (value < 0) s"▲${-value}" else if (value > 0) s"▼$value" else "−0"
  }
  final case class EventPoints(eventName: EventName, pointsMaybe: Option[Points])
  final case class RatingRow(place: Place, trend: Trend, license: License, eventsPoints: List[EventPoints], totalPoints: Points) 

  final case class Rating(header: Header, rows: List[RatingRow])

  sealed trait Gender
  object Gender {
    case object Men extends Gender
    case object Women extends Gender

    def fromString(str: String): Gender = str match {
      case "Мужской" => Men
      case "Женский" => Women
    }
  }

}
