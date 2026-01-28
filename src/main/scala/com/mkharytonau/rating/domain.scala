package com.mkharytonau.rating

import io.estatico.newtype.macros.newtype
import io.estatico.newtype.ops._
import com.mkharytonau.rating.domain.License.FIOInRussian
import com.mkharytonau.rating.domain.License.FIOInEnglish
import scala.concurrent.duration.FiniteDuration
import derevo.derive
import derevo.circe.encoder
import io.circe._
import com.mkharytonau.rating.domain.AmountOfParticipations.BestParticipant
import cats.Show
import cats.syntax.option._

object domain {

  sealed trait Gender
  object Gender {
    case object Men extends Gender
    case object Women extends Gender

    def fromString(str: String): Option[Gender] = PartialFunction.condOpt(str.toLowerCase) {
      case "мужской" => Men
      case "женский" => Women
    }

    implicit val genderShow: Show[Gender] = Show.show {
      case Men   => "men"
      case Women => "women"
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
  final case class AG(from: Age, to: Option[Age]) {
    def show: String = s"${from.value}" + to
      .map(toAge => s"-${toAge.value}")
      .getOrElse("+")
  }
  object AG {
    def fromString(str: String): Option[AG] = {
      val trimmed = str.trim()

      def fromTo(fromStr: String, toStr: Option[String]): Option[AG] =
        toStr match {
          case Some(toStr) =>
            toStr.toIntOption.flatMap(to =>
              fromStr.toIntOption.map { from =>
                AG(Age(from), Some(Age(to)))
              }
            )
          case None =>
            fromStr.toIntOption.map { from =>
              AG(Age(from), None)
            }
        }

      PartialFunction
        .condOpt(trimmed) {
          case s"М$fromStr-$toStr" => fromTo(fromStr, toStr.some)
          case s"Ж$fromStr-$toStr" => fromTo(fromStr, toStr.some)
          case s"М$fromStr+"       => fromTo(fromStr, None)
          case s"Ж$fromStr+"       => fromTo(fromStr, None)
        }
        .flatten
    }
  }

  final case class License(
      id: LicenseId,
      fioInRussian: FIOInRussian,
      fioInEnglish: FIOInEnglish,
      gender: Gender,
      ag: AG,
      club: Option[Club]
  )
  object License {
    @derive(encoder)
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

  final case class EventResultCalculation(
      license: License,
      place: Place,
      points: Points
  )

  final case class EventResultWithCalculation(
      result: EventResult,
      calculation: Option[EventResultCalculation]
  )

  final case class EventResultsCalculated(
      results: EventResults,
      calculated: List[EventResultWithCalculation]
  )

  final case class EventConfig(
      name: EventName,
      eventCategory: EventCategory,
      resultsPath: ResourcePath,
      resultsLoader: EventResultsReader,
      resultCalculator: EventResultsCalculator,
      ratingBase: Double
  )
  final case class CompetitionConfig(
      name: CompetitionName,
      events: List[EventConfig]
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
    case object Stayer extends EventCategory
    case object Duathlon extends EventCategory
    case object Multi extends EventCategory
  }

  final case class EventCalculated(
      name: EventName,
      eventCategory: EventCategory,
      results: EventResultsCalculated
  )
  final case class CompetitionCalculated(
      events: List[EventCalculated]
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

  final case class Rating(
      header: Header,
      rows: List[RatingRow],
      winnerPoints: Option[Points]
  )

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
  final case class AmountOfParticipations(
      histogram: Map[Int, Int], // key = amount of participations, value = amount of athletes with such amount of participations,
      bestParticipants: List[
        BestParticipant
      ] // top N athletes with the most participations
  )
  object AmountOfParticipations {
    @derive(encoder)
    final case class BestParticipant(
        fioInRussian: FIOInRussian,
        participations: Int
    )
  }

  @derive(encoder)
  final case class Statistics(
      unique: UniqueStatistics,
      total: TotalStatistics,
      events: List[EventStatistics],
      amountOfParticipations: AmountOfParticipations
  )
}
