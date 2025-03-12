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

  final case class License(
      id: LicenseId,
      fioInRussian: FIOInRussian,
      fioInEnglish: FIOInEnglish,
      club: Option[Club]
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

  final case class EventConfig(
      resultsPath: ResourcePath,
      resultsLoader: EventResults,
      ratingBase: Double
  )

  @newtype case class CompetitionName(value: String)
  final case class CompetitionConfig(
      name: CompetitionName,
      events: List[EventConfig]
  )

}
