package com.mkharytonau.rating

import io.estatico.newtype.macros.newtype
import io.estatico.newtype.ops._
import com.mkharytonau.rating.domain.License.FIOInRussian
import com.mkharytonau.rating.domain.License.FIOInEnglish
import scala.concurrent.duration.FiniteDuration
import derevo.derive
import derevo.circe.encoder
import io.circe._

object domain {

  sealed trait Gender
  object Gender {
    case object Men extends Gender
    case object Women extends Gender

    def fromString(str: String): Gender = str match {
      case "Мужской" => Men
      case "Женский" => Women
    }
  }

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
  @newtype case class Age(value: Int)
  final case class AG(from: Age, to: Age) {
    def show: String = s"AG ${from.value}-${to.value}"
  }
  object AG {
    def fromString(str: String): Option[AG] = {
      val trimmed = str.trim()
      val s"AG $fromStr-$toStr" = trimmed
      fromStr.toIntOption.flatMap { from =>
        toStr.toIntOption.map { to =>
          AG(Age(from), Age(to))
        }
      }
    }
  }

  final case class License(
      id: LicenseId,
      fioInRussian: FIOInRussian,
      fioInEnglish: FIOInEnglish,
      gender: Gender,
      ag: AG,
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
      eventCategory: EventCategory,
      resultsPath: ResourcePath,
      resultsLoader: EventResultsReader,
      calculatedPathCsv: ResourcePath,
      calculatedPathHtml: ResourcePath,
      resultCalculator: EventResultsCalculator[C, A],
      ratingBase: Double
  )
  final case class CompetitionConfig[C, A](
      name: CompetitionName,
      events: List[EventConfig[C, A]]
  )

  // rating
  @newtype case class CompetitionName(value: String)
  case class EventName(ratingName: String, jsCalculatorName: String)
  object EventName {
    implicit val eventNameEncoder: Encoder[EventName] = Encoder.instance { en =>
      Json.fromString(en.ratingName)
    }
  }

  sealed trait EventCategory
  object EventCategory {
    case object Sprint extends EventCategory
    case object Olympic extends EventCategory
    case object Duathlon extends EventCategory
    case object Kross extends EventCategory
    case object HalfIronmen extends EventCategory
    case object Ironmen extends EventCategory
  }

  final case class EventCalculated[A](
      name: EventName,
      eventCategory: EventCategory,
      results: EventResultsCalculated[Unit]
  )
  final case class CompetitionCalculated(
      events: List[EventCalculated[Unit]]
  )

  @newtype case class Trend(value: Int) {
    def show: String =
      if (value < 0) s"▲${-value}" else if (value > 0) s"▼$value" else "−0"
  }
  final case class EventPoints(
      eventName: EventName,
      eventCategory: EventCategory,
      pointsMaybe: Option[Points]
  )
  final case class RatingRow(
      place: Place,
      trend: Trend,
      license: License,
      placeAG: Option[Place],
      eventsPoints: List[EventPoints],
      totalPoints: Points,
      theBestTrend: Boolean
  )

  final case class Rating(header: Header, rows: List[RatingRow], winnerPoints: Option[Points])

  // statistics
  @derive(encoder)
  final case class TotalStatistics(
      participants: Int,
      categories: Categories
  )
  @derive(encoder)
  final case class Categories(
      license: Categories.ByLicense,
      ageGroup: Map[AG, Int],
      club: Map[Club, Int]
  )
  object Categories {
    @derive(encoder)
    final case class ByLicense(licensed: Int, unlicensed: Int)

    implicit val agKeyEncoder: KeyEncoder[AG] = KeyEncoder.instance(_.show)
    implicit val clubKeyEncoder: KeyEncoder[Club] = KeyEncoder.instance(_.value)
  }

  @derive(encoder)
  final case class EventStatistics(
      name: EventName,
      participants: Int,
      categories: Categories
  )

  @derive(encoder)
  final case class UniqueStatistics(
      licenses: Int
  )

  @derive(encoder)
  final case class Statistics(
      unique: UniqueStatistics,
      total: TotalStatistics,
      events: List[EventStatistics]
  )
}
