package com.mkharytonau.rating

import com.github.tototoshi.csv._
import java.io.File
import scala.io.Source
import scala.concurrent.duration.FiniteDuration
import scala.concurrent.duration._
import scala.util.{Try, Failure, Success}
import com.mkharytonau.rating.domain._

object MainWomen extends App {
  val licenses = Licenses.loadWomen(ResourcePath("2025/licenses.csv"))
  val nameMapping = NameMapping.load(ResourcePath("2025/name_mapping.csv"))

  val competitionConfigs = List(
    CompetitionConfig(
      name = CompetitionName("Зимний триатлон. Логойск"),
      events = List(
        EventConfig[Any, Unit](
          EventName("Зимний триатлон"),
          ResourcePath("2025/Logoisk_winter_tri/women_tri_raw.csv"),
          EventResultsReader.OBelarus,
          ResourcePath("2025/Logoisk_winter_tri/women_tri_calculated.csv"),
          EventResultsCalculator.Standart,
          700.0
        ),
        EventConfig[Any, Unit](
          EventName("Зимний дуатлон"),
          ResourcePath("2025/Logoisk_winter_tri/women_duo_raw.csv"),
          EventResultsReader.OBelarus,
          ResourcePath("2025/Logoisk_winter_tri/women_duo_calculated.csv"),
          EventResultsCalculator.Standart,
          650.0
        )
      )
    )
  )

  val competitionsCalculated = competitionConfigs.map { competitionConfig =>
    val eventsCalculated = competitionConfig.events.map { eventConfig =>
      val eventCalculated = eventConfig.resultCalculator.calculate(
        eventConfig.resultsLoader.read(eventConfig.resultsPath),
        eventConfig,
        licenses,
        nameMapping,
        ()
      )
      EventResultsWriter.Standart.write( // TODO implicit side effect
        eventCalculated,
        eventConfig.calculatedPath
      )

      EventCalculated[Unit](eventConfig.name, eventCalculated)
    }

    CompetitionCalculated(eventsCalculated)
  }

  val rating = RatingCalculator.Standart2025.calculate(
    licenses,
    competitionsCalculated
  )

  RatingWriter.Standart.write(rating, ResourcePath("2025/rating_women.csv"))
}
