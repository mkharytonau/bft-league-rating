package com.mkharytonau.rating

import com.github.tototoshi.csv._
import java.io.File
import scala.io.Source
import scala.concurrent.duration.FiniteDuration
import scala.concurrent.duration._
import scala.util.{Try, Failure, Success}
import com.mkharytonau.rating.domain._

object MainMen extends App {
  val licenses = Licenses.loadMen(ResourcePath("2025/licenses.csv"))
  val nameMapping = NameMapping.load(ResourcePath("2025/name_mapping.csv"))

  val competitionConfigs = List(
    CompetitionConfig(
      name = CompetitionName("Зимний триатлон. Логойск"),
      events = List(
        EventConfig[Any, Unit](
          EventName("Зимний триатлон", "LogoiskWinterTri"),
          EventCategory.Kross,
          ResourcePath("2025/Logoisk_winter_tri/men_tri_raw.csv"),
          EventResultsReader.OBelarus,
          ResourcePath("2025/Logoisk_winter_tri/men_tri_calculated.csv"),
          ResourcePath("2025/Logoisk_winter_tri/men_tri_calculated.html"),
          EventResultsCalculator.Standart,
          700.0
        ),
        EventConfig[Any, Unit](
          EventName("Зимний дуатлон", "LogoiskWinterDuo"),
          EventCategory.Kross,
          ResourcePath("2025/Logoisk_winter_tri/men_duo_raw.csv"),
          EventResultsReader.OBelarus,
          ResourcePath("2025/Logoisk_winter_tri/men_duo_calculated.csv"),
          ResourcePath("2025/Logoisk_winter_tri/men_duo_calculated.html"),
          EventResultsCalculator.Standart,
          650.0
        )
      )
    ),
    CompetitionConfig(
      name = CompetitionName("Минск Индор Триатлон"),
      events = List(
        EventConfig[Any, Unit](
          EventName("Индор триатлон", "MinskIndoorTriathlon"),
          EventCategory.Kross,
          ResourcePath("2025/Minsk_Indoor_Triathlon/men.csv"),
          EventResultsReader.MinskIndoorTriathlon,
          ResourcePath("2025/Minsk_Indoor_Triathlon/men_calculated.csv"),
          ResourcePath("2025/Minsk_Indoor_Triathlon/men_calculated.html"),
          EventResultsCalculator.Standart,
          700.0
        ),
      )
    ),
    CompetitionConfig(
      name = CompetitionName("Дрогичин Дуатлон"),
      events = List(
        EventConfig[Any, Unit](
          EventName("Дрогичин Дуатлон", "DuathlonDrogichin"),
          EventCategory.Duathlon,
          ResourcePath("2025/Drogichin/men.csv"),
          EventResultsReader.Athlinks,
          ResourcePath("2025/Drogichin/men_calculated.csv"),
          ResourcePath("2025/Drogichin/men_calculated.html"),
          EventResultsCalculator.Standart,
          700.0
        ),
      )
    ),
    CompetitionConfig(
      name = CompetitionName("МогиЛев"),
      events = List(
        EventConfig[Any, Unit](
          EventName("МогиЛев", "TriathlonMogiLev"),
          EventCategory.Sprint,
          ResourcePath("2025/Mogilev/men.csv"),
          EventResultsReader.Athlinks,
          ResourcePath("2025/Mogilev/men_calculated.csv"),
          ResourcePath("2025/Mogilev/men_calculated.html"),
          EventResultsCalculator.Standart,
          700.0
        ),
      )
    ),
    CompetitionConfig(
      name = CompetitionName("Браслав"),
      events = List(
        EventConfig[Any, Unit](
          EventName("Браслав", "KrossTriathlonBraslav"),
          EventCategory.Sprint,
          ResourcePath("2025/BraslavKross/men.csv"),
          EventResultsReader.OBelarus2,
          ResourcePath("2025/BraslavKross/men_calculated.csv"),
          ResourcePath("2025/BraslavKross/men_calculated.html"),
          EventResultsCalculator.Standart,
          700.0
        ),
      )
    ),
    CompetitionConfig(
      name = CompetitionName("Брест акватлон"),
      events = List(
        EventConfig[Any, Unit](
          EventName("Брест акватлон", "BrestAquathlon"),
          EventCategory.Duathlon,
          ResourcePath("2025/BrestAquathlon/men.csv"),
          EventResultsReader.OBelarus,
          ResourcePath("2025/BrestAquathlon/men_calculated.csv"),
          ResourcePath("2025/BrestAquathlon/men_calculated.html"),
          EventResultsCalculator.Standart,
          700.0
        ),
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
      EventResultsWriter.CSV.write( // TODO implicit side effect
        eventCalculated,
        eventConfig
      )
      EventResultsWriter.HTML.write( // TODO implicit side effect
        eventCalculated,
        eventConfig
      )

      EventCalculated[Unit](eventConfig.name, eventConfig.eventCategory, eventCalculated)
    }

    CompetitionCalculated(eventsCalculated)
  }

  val rating = RatingCalculator.Standart2025.calculate(
    licenses,
    competitionsCalculated
  )

  RatingWriter.CSV.write(rating, ResourcePath("2025/rating_men.csv"))
  RatingWriter.HTML.write(rating, ResourcePath("2025/rating_men.html"))
}
